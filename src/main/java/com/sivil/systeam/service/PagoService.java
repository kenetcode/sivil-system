package com.sivil.systeam.service;

import com.sivil.systeam.dto.VentaTemporalDTO;
import com.sivil.systeam.entity.*;
import com.sivil.systeam.enums.*;
import com.sivil.systeam.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PagoService {

    @Autowired private PagoRepository pagoRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private DetalleVentaRepository detalleVentaRepository;
    @Autowired private LibroRepository libroRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    /* ==========================================================
     *  EFECTIVO (ya lo tenías)
     * ========================================================== */

    // 1) Pago en efectivo para una venta ya registrada
    @Transactional
    public Pago procesarPagoEfectivoVenta(Integer idVenta,
                                          BigDecimal montoRecibido,
                                          String observaciones,
                                          String vendedorEmail) {

        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la venta con ID: " + idVenta));

        if (montoRecibido == null || montoRecibido.compareTo(venta.getTotal()) < 0) {
            throw new IllegalArgumentException("El monto recibido es insuficiente para cubrir el total de la venta.");
        }

        venta.setEstado(EstadoVenta.finalizada);
        venta.setTipo_pago(MetodoPago.efectivo);
        ventaRepository.save(venta);

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setMonto(montoRecibido);
        pago.setMetodo_pago(MetodoPago.efectivo);
        pago.setEstado_pago(EstadoPago.completado);
        pago.setFecha_pago(LocalDateTime.now());
        pago.setReferencia_transaccion("EFEC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (observaciones != null && !observaciones.isBlank()) {
            pago.setDatos_tarjeta_encriptados("OBS: " + observaciones);
        }
        return pagoRepository.save(pago);
    }

    // 2) Pago en efectivo desde venta temporal (pendiente en sesión)
    @Transactional
    public Pago procesarPagoEfectivoVentaPendiente(VentaTemporalDTO ventaTemp,
                                                   BigDecimal montoRecibido,
                                                   String observaciones,
                                                   String vendedorEmail) {

        if (ventaTemp == null) {
            throw new IllegalArgumentException("No se recibió información de la venta temporal.");
        }
        if (montoRecibido == null || montoRecibido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto recibido debe ser mayor que cero.");
        }

        // Vendedor
        Usuario vendedor = ventaTemp.getVendedor();
        if (vendedor == null && vendedorEmail != null && !vendedorEmail.isBlank()) {
            vendedor = usuarioRepository.findByEmail(vendedorEmail).orElse(null);
        }

        // Crear venta
        Venta nuevaVenta = new Venta();
        nuevaVenta.setNumero_factura(ventaTemp.getNumeroFactura());
        nuevaVenta.setFecha_venta(ventaTemp.getFechaVenta() != null ? ventaTemp.getFechaVenta() : LocalDateTime.now());
        nuevaVenta.setNombre_cliente(ventaTemp.getNombreCliente());
        nuevaVenta.setContacto_cliente(ventaTemp.getContactoCliente());
        nuevaVenta.setIdentificacion_cliente(ventaTemp.getIdentificacionCliente());
        nuevaVenta.setSubtotal(ventaTemp.getSubtotal());
        nuevaVenta.setImpuestos(ventaTemp.getImpuestos());
        nuevaVenta.setDescuento_aplicado(ventaTemp.getDescuentoAplicado());
        nuevaVenta.setTotal(ventaTemp.getTotal());
        nuevaVenta.setTipo_pago(MetodoPago.efectivo);
        nuevaVenta.setEstado(EstadoVenta.finalizada);
        nuevaVenta.setVendedor(vendedor);

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // Detalles + stock
        for (VentaTemporalDTO.DetalleVentaTemporalDTO det : ventaTemp.getDetallesVenta()) {
            Libro libro = libroRepository.findById(det.getIdLibro())
                    .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado: " + det.getTituloLibro()));
            if (libro.getCantidad_stock() < det.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + libro.getTitulo());
            }
            libro.setCantidad_stock(libro.getCantidad_stock() - det.getCantidad());
            libroRepository.save(libro);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setLibro(libro);
            detalle.setCantidad(det.getCantidad());
            detalle.setPrecio_unitario(det.getPrecioUnitario());
            detalle.setSubtotal_item(det.getSubtotalItem());
            detalleVentaRepository.save(detalle);
        }

        // Pago
        Pago pago = new Pago();
        pago.setVenta(ventaGuardada);
        pago.setMetodo_pago(MetodoPago.efectivo);
        pago.setMonto(montoRecibido);
        pago.setEstado_pago(EstadoPago.completado);
        pago.setFecha_pago(LocalDateTime.now());
        pago.setReferencia_transaccion("EFEC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        if (observaciones != null && !observaciones.isBlank()) {
            pago.setDatos_tarjeta_encriptados("OBS: " + observaciones);
        }
        Pago pagoGuardado = pagoRepository.save(pago);

        ventaGuardada.setEstado(EstadoVenta.finalizada);
        ventaRepository.save(ventaGuardada);

        return pagoGuardado;
    }

    /* ==========================================================
     *  TARJETA (agregado para alinear con tu Controller)
     * ========================================================== */

    // A) TARJETA: desde VENTA TEMPORAL (ventaPendiente=true)
    @Transactional
    public Pago procesarPagoConVentaPendiente(String numeroTarjeta,
                                              String fechaVencimiento,
                                              String cvv,
                                              String nombreTitular,
                                              String email,
                                              String direccion,
                                              Pago pagoRequest,
                                              VentaTemporalDTO ventaTemp) {

        if (ventaTemp == null) {
            throw new IllegalArgumentException("No se recibió información de la venta temporal.");
        }
        if (pagoRequest == null || pagoRequest.getMonto() == null) {
            throw new IllegalArgumentException("El monto del pago es requerido.");
        }

        // Vendedor
        Usuario vendedor = ventaTemp.getVendedor();

        // Crear venta
        Venta nuevaVenta = new Venta();
        nuevaVenta.setNumero_factura(ventaTemp.getNumeroFactura());
        nuevaVenta.setFecha_venta(ventaTemp.getFechaVenta() != null ? ventaTemp.getFechaVenta() : LocalDateTime.now());
        nuevaVenta.setNombre_cliente(ventaTemp.getNombreCliente());
        nuevaVenta.setContacto_cliente(ventaTemp.getContactoCliente());
        nuevaVenta.setIdentificacion_cliente(ventaTemp.getIdentificacionCliente());
        nuevaVenta.setSubtotal(ventaTemp.getSubtotal());
        nuevaVenta.setImpuestos(ventaTemp.getImpuestos());
        nuevaVenta.setDescuento_aplicado(ventaTemp.getDescuentoAplicado());
        nuevaVenta.setTotal(ventaTemp.getTotal());
        nuevaVenta.setTipo_pago(MetodoPago.tarjeta);
        nuevaVenta.setEstado(EstadoVenta.finalizada);
        nuevaVenta.setVendedor(vendedor);

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        // Detalles + stock
        for (VentaTemporalDTO.DetalleVentaTemporalDTO det : ventaTemp.getDetallesVenta()) {
            Libro libro = libroRepository.findById(det.getIdLibro())
                    .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado: " + det.getTituloLibro()));
            if (libro.getCantidad_stock() < det.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para: " + libro.getTitulo());
            }
            libro.setCantidad_stock(libro.getCantidad_stock() - det.getCantidad());
            libroRepository.save(libro);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(ventaGuardada);
            detalle.setLibro(libro);
            detalle.setCantidad(det.getCantidad());
            detalle.setPrecio_unitario(det.getPrecioUnitario());
            detalle.setSubtotal_item(det.getSubtotalItem());
            detalleVentaRepository.save(detalle);
        }

        // Pago tarjeta
        Pago pago = new Pago();
        pago.setVenta(ventaGuardada);
        pago.setMetodo_pago(MetodoPago.tarjeta);
        pago.setMonto(pagoRequest.getMonto());
        pago.setEstado_pago(EstadoPago.completado);
        pago.setFecha_pago(LocalDateTime.now());
        pago.setReferencia_transaccion("CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Guardar de forma segura/mascada (aquí de ejemplo)
        String ult4 = numeroTarjeta != null && numeroTarjeta.length() >= 4
                ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : "****";
        pago.setDatos_tarjeta_encriptados("****-****-****-" + ult4);
        return pagoRepository.save(pago);
    }

    // B) TARJETA: para una venta ya existente (idVenta != null)
    @Transactional
    public Pago procesarPago(String numeroTarjeta,
                             String fechaVencimiento,
                             String cvv,
                             String nombreTitular,
                             String email,
                             String direccion,
                             Pago pagoRequest,
                             Integer idCompra,   // no implementado aquí
                             Integer idVenta) {

        if (idVenta == null) {
            throw new IllegalArgumentException("Falta idVenta (flujo de compra online no implementado aquí).");
        }

        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la venta con ID: " + idVenta));

        venta.setTipo_pago(MetodoPago.tarjeta);
        venta.setEstado(EstadoVenta.finalizada);
        ventaRepository.save(venta);

        Pago pago = new Pago();
        pago.setVenta(venta);
        pago.setMetodo_pago(MetodoPago.tarjeta);
        pago.setMonto(pagoRequest != null ? pagoRequest.getMonto() : venta.getTotal());
        pago.setEstado_pago(EstadoPago.completado);
        pago.setFecha_pago(LocalDateTime.now());
        pago.setReferencia_transaccion("CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        String ult4 = numeroTarjeta != null && numeroTarjeta.length() >= 4
                ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : "****";
        pago.setDatos_tarjeta_encriptados("****-****-****-" + ult4);

        return pagoRepository.save(pago);
    }

    // C) TARJETA: compra online pendiente (firma para que tu controlador compile)
    //    Implementa aquí tu lógica real cuando tengas las entidades de compra online.
    @Transactional
    public Pago procesarPagoConCompraPendiente(String numeroTarjeta,
                                               String fechaVencimiento,
                                               String cvv,
                                               String nombreTitular,
                                               String email,
                                               String direccion,
                                               Pago pagoRequest,
                                               Object compraTemporalDTO /* reemplaza por tu DTO real */) {
        throw new UnsupportedOperationException(
                "procesarPagoConCompraPendiente aún no está implementado en este módulo.");
    }
}
