package com.sivil.systeam.service;

import com.sivil.systeam.entity.*;
import com.sivil.systeam.dto.VentaTemporalDTO;
import com.sivil.systeam.dto.CompraTemporalDTO;
import com.sivil.systeam.enums.EstadoPago;
import com.sivil.systeam.enums.EstadoVenta;
import com.sivil.systeam.enums.EstadoCompra;
import com.sivil.systeam.enums.MetodoPago;
import com.sivil.systeam.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.Optional;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final LibroRepository libroRepository;
    private final CompraOnlineRepository compraOnlineRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final UsuarioRepository usuarioRepository;

    private static final Pattern NUMERO_TARJETA_PATTERN = Pattern.compile("\\d{16}");
    private static final Pattern FECHA_VENCIMIENTO_PATTERN = Pattern.compile("(0[1-9]|1[0-2])/\\d{2}");
    private static final Pattern CVV_PATTERN = Pattern.compile("\\d{3}");

    public PagoService(PagoRepository pagoRepository, VentaRepository ventaRepository,
                      DetalleVentaRepository detalleVentaRepository, LibroRepository libroRepository,
                      CompraOnlineRepository compraOnlineRepository, DetalleCompraRepository detalleCompraRepository,
                      UsuarioRepository usuarioRepository) {
        this.pagoRepository = pagoRepository;
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.libroRepository = libroRepository;
        this.compraOnlineRepository = compraOnlineRepository;
        this.detalleCompraRepository = detalleCompraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Pago procesarPago(String numeroTarjeta, String fechaVencimiento, String cvv, 
                           String nombreTitular, String email, String direccion, 
                           Pago pago, Integer idCompra, Integer idVenta) {
        
        // Validar formato de datos de tarjeta
        validarDatosTarjeta(numeroTarjeta, fechaVencimiento, cvv, nombreTitular, email);
        
        // Simular datos encriptados (solo para demostración)
        String datosSimulados = simularEncriptacion(numeroTarjeta, fechaVencimiento, cvv);
        pago.setDatos_tarjeta_encriptados(datosSimulados);
        
        // Configurar pago
        pago.setMetodo_pago(MetodoPago.tarjeta);
        pago.setEstado_pago(EstadoPago.completado);
        pago.setReferencia_transaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Asociar el pago con la venta o compra correspondiente
        if (idVenta != null) {
            // Buscar la venta y asociarla al pago
            Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + idVenta));
            pago.setVenta(venta);
        } else if (idCompra != null) {
            // TODO: Implementar lógica para compras cuando sea necesario
            throw new IllegalArgumentException("Procesamiento de compras no implementado aún");
        } else {
            // Si no viene ni venta ni compra, es un pago independiente (no permitido por el constraint)
            throw new IllegalArgumentException("Debe especificar una venta o compra para procesar el pago (La vista [pago con tarjeta] desde esta pestaña [Gestión de Pagos] es solo demostrativa, para ver su funcionalidad real, inicie el proceso de venta en la pestaña [Realizar Venta] o el proceso de compra desde [Compras Online])");
        }

        return pagoRepository.save(pago);
    }

    /**
     * Procesa pago y crea la venta desde datos temporales almacenados en sesión
     */
    @Transactional
    public Pago procesarPagoConVentaPendiente(String numeroTarjeta, String fechaVencimiento, String cvv,
                                            String nombreTitular, String email, String direccion,
                                            Pago pago, VentaTemporalDTO ventaTemporal) {

        // 1. Validar datos de tarjeta
        validarDatosTarjeta(numeroTarjeta, fechaVencimiento, cvv, nombreTitular, email);

        // 2. Verificar que el número de factura no exista
        if (ventaRepository.existsByNumeroFactura(ventaTemporal.getNumeroFactura())) {
            throw new IllegalArgumentException("El número de factura ya existe: " + ventaTemporal.getNumeroFactura());
        }

        // 3. Verificar stock disponible para todos los libros antes de crear la venta
        for (VentaTemporalDTO.DetalleVentaTemporalDTO detalleTemporal : ventaTemporal.getDetallesVenta()) {
            Optional<Libro> libroOpt = libroRepository.findById(detalleTemporal.getIdLibro());
            if (libroOpt.isEmpty()) {
                throw new IllegalArgumentException("Libro no encontrado: " + detalleTemporal.getIdLibro());
            }

            Libro libro = libroOpt.get();
            if (libro.getCantidad_stock() < detalleTemporal.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + libro.getTitulo() +
                        ". Stock disponible: " + libro.getCantidad_stock() +
                        ". Cantidad solicitada: " + detalleTemporal.getCantidad());
            }
        }

        // 4. Crear la venta en la base de datos
        Venta nuevaVenta = new Venta();
        nuevaVenta.setNumero_factura(ventaTemporal.getNumeroFactura());
        nuevaVenta.setVendedor(ventaTemporal.getVendedor());
        nuevaVenta.setNombre_cliente(ventaTemporal.getNombreCliente());
        nuevaVenta.setContacto_cliente(ventaTemporal.getContactoCliente());
        nuevaVenta.setIdentificacion_cliente(ventaTemporal.getIdentificacionCliente());
        nuevaVenta.setSubtotal(ventaTemporal.getSubtotal());
        nuevaVenta.setDescuento_aplicado(ventaTemporal.getDescuentoAplicado());
        nuevaVenta.setImpuestos(ventaTemporal.getImpuestos());
        nuevaVenta.setTotal(ventaTemporal.getTotal());
        nuevaVenta.setTipo_pago(ventaTemporal.getTipoPago());
        nuevaVenta.setEstado(ventaTemporal.getEstado());
        nuevaVenta.setFecha_venta(ventaTemporal.getFechaVenta());

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // 5. Crear detalles de venta y actualizar stock
        for (VentaTemporalDTO.DetalleVentaTemporalDTO detalleTemporal : ventaTemporal.getDetallesVenta()) {
            Libro libro = libroRepository.findById(detalleTemporal.getIdLibro()).get();

            // Crear detalle de venta
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setLibro(libro);
            detalle.setCantidad(detalleTemporal.getCantidad());
            detalle.setPrecio_unitario(detalleTemporal.getPrecioUnitario());
            detalle.setSubtotal_item(detalleTemporal.getSubtotalItem());

            detalleVentaRepository.save(detalle);

            // Actualizar stock del libro
            libro.setCantidad_stock(libro.getCantidad_stock() - detalleTemporal.getCantidad());
            libroRepository.save(libro);
        }

        // 6. Procesar el pago
        String datosSimulados = simularEncriptacion(numeroTarjeta, fechaVencimiento, cvv);
        pago.setDatos_tarjeta_encriptados(datosSimulados);
        pago.setMetodo_pago(MetodoPago.tarjeta);
        pago.setEstado_pago(EstadoPago.completado);
        pago.setReferencia_transaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setVenta(ventaGuardada);

        return pagoRepository.save(pago);
    }

    /**
     * Procesa pago y crea la compra online desde datos temporales almacenados en sesión
     */
    @Transactional
    public Pago procesarPagoConCompraPendiente(String numeroTarjeta, String fechaVencimiento, String cvv,
                                             String nombreTitular, String email, String direccion,
                                             Pago pago, CompraTemporalDTO compraTemporal) {

        // 1. Validar datos de tarjeta
        validarDatosTarjeta(numeroTarjeta, fechaVencimiento, cvv, nombreTitular, email);

        // 2. Verificar que el número de orden no exista
        if (compraOnlineRepository.findByNumeroOrden(compraTemporal.getNumeroOrden()).isPresent()) {
            throw new IllegalArgumentException("El número de orden ya existe: " + compraTemporal.getNumeroOrden());
        }

        // 3. El usuario ya está autenticado, obtenerlo desde la sesión de Spring Security
        // No necesitamos crear un usuario temporal ya que el usuario está logueado
        // El usuario será asignado desde el controlador que tiene acceso a la sesión

        // 4. Verificar stock disponible para todos los libros antes de crear la compra
        for (CompraTemporalDTO.DetalleCompraTemporalDTO detalleTemporal : compraTemporal.getDetallesCompra()) {
            Optional<Libro> libroOpt = libroRepository.findById(detalleTemporal.getLibroId());
            if (libroOpt.isEmpty()) {
                throw new IllegalArgumentException("Libro no encontrado: " + detalleTemporal.getLibroId());
            }

            Libro libro = libroOpt.get();
            if (libro.getCantidad_stock() < detalleTemporal.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + libro.getTitulo() +
                        ". Stock disponible: " + libro.getCantidad_stock() +
                        ". Cantidad solicitada: " + detalleTemporal.getCantidad());
            }
        }

        // 5. Crear la compra online en la base de datos
        CompraOnline nuevaCompra = new CompraOnline();
        nuevaCompra.setNumero_orden(compraTemporal.getNumeroOrden());

        nuevaCompra.setComprador(compraTemporal.getComprador());
        nuevaCompra.setSubtotal(compraTemporal.getSubtotal());
        nuevaCompra.setImpuestos(compraTemporal.getImpuestos());
        nuevaCompra.setTotal(compraTemporal.getTotal());
        nuevaCompra.setDireccion_entrega(compraTemporal.getDireccionEntrega());
        nuevaCompra.setEstado_compra(EstadoCompra.procesada); // La compra se procesa al pagar
        nuevaCompra.setMetodo_pago(compraTemporal.getMetodoPago());

        CompraOnline compraGuardada = compraOnlineRepository.save(nuevaCompra);

        // 6. Crear detalles de compra y actualizar stock
        for (CompraTemporalDTO.DetalleCompraTemporalDTO detalleTemporal : compraTemporal.getDetallesCompra()) {
            Libro libro = libroRepository.findById(detalleTemporal.getLibroId()).get();

            // Crear detalle de compra
            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compraGuardada);
            detalle.setLibro(libro);
            detalle.setCantidad(detalleTemporal.getCantidad());
            detalle.setPrecio_unitario(detalleTemporal.getPrecioUnitario());
            detalle.setSubtotal_item(detalleTemporal.getSubtotal());

            detalleCompraRepository.save(detalle);

            // Actualizar stock del libro
            libro.setCantidad_stock(libro.getCantidad_stock() - detalleTemporal.getCantidad());
            libroRepository.save(libro);
        }

        // 7. Procesar el pago
        String datosSimulados = simularEncriptacion(numeroTarjeta, fechaVencimiento, cvv);
        pago.setDatos_tarjeta_encriptados(datosSimulados);
        pago.setMetodo_pago(MetodoPago.tarjeta);
        pago.setEstado_pago(EstadoPago.completado);
        pago.setReferencia_transaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setCompra(compraGuardada);

        return pagoRepository.save(pago);
    }

    private void validarDatosTarjeta(String numeroTarjeta, String fechaVencimiento, String cvv,
                                   String nombreTitular, String email) {
        
        if (numeroTarjeta == null || !NUMERO_TARJETA_PATTERN.matcher(numeroTarjeta.replaceAll("\\s+", "")).matches()) {
            throw new IllegalArgumentException("El número de tarjeta debe tener exactamente 16 dígitos");
        }
        
        if (fechaVencimiento == null || !FECHA_VENCIMIENTO_PATTERN.matcher(fechaVencimiento).matches()) {
            throw new IllegalArgumentException("La fecha de vencimiento debe tener el formato MM/AA (ejemplo: 12/25)");
        }
        
        // Validar rango de mes y año
        String[] partesFecha = fechaVencimiento.split("/");
        int mes = Integer.parseInt(partesFecha[0]);
        int año = Integer.parseInt("20" + partesFecha[1]); // Convertir AA a 20AA
        
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 01 y 12");
        }
        
        int añoActual = LocalDate.now().getYear();
        int mesActual = LocalDate.now().getMonthValue();
        
        // Validar que no sea una fecha pasada
        // Las tarjetas vencen al final del mes indicado
        if (año < añoActual || (año == añoActual && mes < mesActual)) {
            throw new IllegalArgumentException("La tarjeta no puede estar vencida");
        }
        
        if (año > añoActual + 2) {
            throw new IllegalArgumentException("Fecha de vencimiento muy lejana en el futuro");
        }
        
        if (cvv == null || !CVV_PATTERN.matcher(cvv).matches()) {
            throw new IllegalArgumentException("El CVV debe tener exactamente 3 dígitos");
        }
        
        if (nombreTitular == null || nombreTitular.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del titular es requerido (mínimo 2 caracteres)");
        }
        
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
    }
    
    private String simularEncriptacion(String numeroTarjeta, String fechaVencimiento, String cvv) {
        // En una implementación real, aquí habría encriptación real
        // Por ahora solo guardamos los últimos 4 dígitos y ocultamos el resto
        String ultimosCuatroDigitos = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        return "****-****-****-" + ultimosCuatroDigitos + "|" + fechaVencimiento + "|***";
    }
}
