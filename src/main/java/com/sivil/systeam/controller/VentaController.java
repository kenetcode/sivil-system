package com.sivil.systeam.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivil.systeam.entity.*;
import com.sivil.systeam.repository.*;
import com.sivil.systeam.service.*;
import com.sivil.systeam.dto.VentaTemporalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NumeracionFacturaService numeracionService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private LibroService libroService;

    

    @GetMapping("/crear")
    public String mostrarFormularioCrearVenta(Model model) {
        Venta venta = new Venta();

        String numeroFactura = numeracionService.generarNumeroFactura(ventaRepository);
        venta.setNumero_factura(numeroFactura);

        model.addAttribute("venta", venta);
        model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(com.sivil.systeam.enums.Estado.activo, 0));
        return "venta/crear-venta";
    }

    @GetMapping("/api/libros/{id}/stock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarStock(@PathVariable Integer id) {
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (libroOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("titulo", libroOpt.get().getTitulo());
            response.put("stock", libroOpt.get().getCantidad_stock());
            response.put("disponible", libroOpt.get().getCantidad_stock() > 0);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/listar")
    public String listarVentas(Model model) {
        // Obtener las ventas finalizadas desde el servicio
        List<Venta> ventas = ventaService.listarVentasFinalizadas();

        model.addAttribute("ventas", ventas);
        model.addAttribute("totalVentas", ventas.size());

        // Mantener valores de ejemplo para otros atributos
        model.addAttribute("ventasActivas", 0);
        model.addAttribute("ventasHoy", 0);
        model.addAttribute("promedioVenta", 0);

        return "venta/listar-ventas";
    }

    // cambios
    @GetMapping("/{id}/modificar")
    public String mostrarFormularioModificacion(@PathVariable("id") Integer id, Model model) {
        try {
            // Obtener la venta por id
            Optional<Venta> ventaOpt = ventaService.obtenerVentaPorId(id);
            if (ventaOpt.isEmpty()) {
                model.addAttribute("error", "Venta no encontrada");
                return "redirect:/ventas/listar";
            }
            Venta venta = ventaOpt.get();

            // Obtener todos los libros activos con stock > 0
            List<Libro> libros = libroService.listarTodosActivosConStock();

            model.addAttribute("venta", venta);
            model.addAttribute("libros", libros);
            model.addAttribute("modoEdicion", true); // flag para diferenciar el formulario de creación
            return "venta/modificar-venta"; // nombre del html que crearemos
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "No se pudo cargar la venta para modificar");
            return "redirect:/ventas/listar";
        }
    }


    //cambios
    @PostMapping("/{id}/modificar")
    public String actualizarVenta(
            @PathVariable("id") Integer id,
            @ModelAttribute("venta") Venta ventaActualizada,
            Model model) {

        // 1. Obtener la venta existente
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (ventaOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró la venta.");
            return "redirect:/ventas/listar";
        }
        Venta ventaExistente = ventaOpt.get();

        // 2. Actualizar campos del cliente
        ventaExistente.setNombre_cliente(ventaActualizada.getNombre_cliente());
        ventaExistente.setContacto_cliente(ventaActualizada.getContacto_cliente());
        ventaExistente.setIdentificacion_cliente(ventaActualizada.getIdentificacion_cliente());

        // 3. Actualizar detalles de venta si es necesario
        // (Aquí asumimos que los detalles se envían de la misma forma que en crear-venta)
        // Por ahora, solo recalcularemos totales con los detalles existentes

        List<DetalleVenta> detalles = ventaExistente.getDetallesVenta();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetalleVenta detalle : detalles) {
            BigDecimal subtotalItem = detalle.getPrecio_unitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            detalle.setSubtotal_item(subtotalItem);
            subtotal = subtotal.add(subtotalItem);
        }

        // 4. Recalcular impuestos y total
        BigDecimal impuestos = subtotal.multiply(new BigDecimal("0.13"));
        BigDecimal total = subtotal.add(impuestos);

        ventaExistente.setSubtotal(subtotal);
        ventaExistente.setImpuestos(impuestos);
        ventaExistente.setTotal(total);

        // 5. Guardar cambios
        ventaRepository.save(ventaExistente);

        return "redirect:/ventas/listar";
    }




    // Procesar datos del formulario y almacenar en sesión (NO crear en BD aún)
    @PostMapping("/crear")
    public String procesarFormularioCrearVenta(
            @ModelAttribute("venta") Venta venta,
            @RequestParam("librosData") String librosDataJson,
            HttpSession session,
            Model model) {

        try {
            // 1. Convertir JSON de libros a lista de objetos
            List<LibroVentaRequest> detallesRequest = objectMapper.readValue(
                    librosDataJson,
                    new TypeReference<List<LibroVentaRequest>>() {}
            );

            if (detallesRequest.isEmpty()) {
                model.addAttribute("error", "Debe agregar al menos un libro para realizar la venta.");
                model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(com.sivil.systeam.enums.Estado.activo, 0));
                return "venta/crear-venta";
            }

            // 2. Obtener el usuario vendedor desde el modelo
            Usuario vendedor = (Usuario) model.getAttribute("currentUser");
            if (vendedor == null) {
                model.addAttribute("error", "No se pudo identificar al vendedor. Por favor inicie sesión nuevamente.");
                model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(com.sivil.systeam.enums.Estado.activo, 0));
                return "venta/crear-venta";
            }

            // 3. Verificar stock disponible para todos los libros
            for (LibroVentaRequest detalleRequest : detallesRequest) {
                Optional<Libro> libroOpt = libroRepository.findById(detalleRequest.getId());
                if (libroOpt.isEmpty()) {
                    model.addAttribute("error", "Libro no encontrado: " + detalleRequest.getId());
                    model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(com.sivil.systeam.enums.Estado.activo, 0));
                    return "venta/crear-venta";
                }

                Libro libro = libroOpt.get();
                if (libro.getCantidad_stock() < detalleRequest.getCantidad()) {
                    model.addAttribute("error", "Stock insuficiente para: " + libro.getTitulo() +
                            ". Stock disponible: " + libro.getCantidad_stock() +
                            ". Cantidad solicitada: " + detalleRequest.getCantidad());
                    model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(com.sivil.systeam.enums.Estado.activo, 0));
                    return "venta/crear-venta";
                }
            }

            // 4. Calcular totales de la venta
            BigDecimal subtotalVenta = BigDecimal.ZERO;
            for (LibroVentaRequest detalleRequest : detallesRequest) {
                BigDecimal subtotalItem = detalleRequest.getPrecio().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
                subtotalVenta = subtotalVenta.add(subtotalItem);
            }

            BigDecimal impuestos = subtotalVenta.multiply(new BigDecimal("0.13"));
            BigDecimal totalVenta = subtotalVenta.add(impuestos);

            // 5. Crear VentaTemporalDTO y guardar en sesión
            VentaTemporalDTO ventaTemporal = new VentaTemporalDTO();
            ventaTemporal.setNumeroFactura(numeracionService.generarNumeroFactura(ventaRepository));
            ventaTemporal.setVendedor(vendedor);
            ventaTemporal.setNombreCliente(venta.getNombre_cliente());
            ventaTemporal.setContactoCliente(venta.getContacto_cliente());
            ventaTemporal.setIdentificacionCliente(venta.getIdentificacion_cliente());
            ventaTemporal.setSubtotal(subtotalVenta);
            ventaTemporal.setDescuentoAplicado(BigDecimal.ZERO);
            ventaTemporal.setImpuestos(impuestos);
            ventaTemporal.setTotal(totalVenta);
            ventaTemporal.setTipoPago(com.sivil.systeam.enums.MetodoPago.tarjeta);
            ventaTemporal.setEstado(com.sivil.systeam.enums.EstadoVenta.activa);
            ventaTemporal.setFechaVenta(LocalDateTime.now());

            // 6. Crear lista de detalles temporales
            List<VentaTemporalDTO.DetalleVentaTemporalDTO> detallesTemporal = new ArrayList<>();
            for (LibroVentaRequest detalleRequest : detallesRequest) {
                VentaTemporalDTO.DetalleVentaTemporalDTO detalleTemporal =
                    new VentaTemporalDTO.DetalleVentaTemporalDTO(
                        detalleRequest.getId(),
                        detalleRequest.getTitulo(),
                        detalleRequest.getCantidad(),
                        detalleRequest.getPrecio(),
                        detalleRequest.getPrecio().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()))
                    );
                detallesTemporal.add(detalleTemporal);
            }
            ventaTemporal.setDetallesVenta(detallesTemporal);

            // 7. Guardar en sesión
            session.setAttribute("ventaPendiente", ventaTemporal);

            // 8. Redirigir a pago con tarjeta (sin idVenta porque aún no existe)
            return "redirect:/pago/tarjeta?monto=" + totalVenta + "&ventaPendiente=true";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar la venta: " + e.getMessage());
            model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(com.sivil.systeam.enums.Estado.activo, 0));
            return "venta/crear-venta";
        }
    }

    // Clase interna para mapear el JSON del frontend
    public static class LibroVentaRequest {
        private Integer id;
        private String titulo;
        private BigDecimal precio;
        private Integer cantidad;

        // Getters y Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }


}