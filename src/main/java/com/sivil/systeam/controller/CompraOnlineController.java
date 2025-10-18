package com.sivil.systeam.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivil.systeam.entity.*;
import com.sivil.systeam.repository.*;
import com.sivil.systeam.service.NumeracionFacturaService;
import com.sivil.systeam.dto.CompraTemporalDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@Controller
@RequestMapping("/compra-online")
public class CompraOnlineController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private CompraOnlineRepository compraOnlineRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NumeracionFacturaService numeracionService;

    @GetMapping("/crear")
    public String mostrarFormularioComprarLibros(Model model, HttpSession session) {
        // Obtener carrito de la sesión
        @SuppressWarnings("unchecked")
        List<com.sivil.systeam.controller.CatalogoController.ItemCarrito> carrito = 
            (List<com.sivil.systeam.controller.CatalogoController.ItemCarrito>) session.getAttribute("carrito");
        
        // Si hay items en el carrito, los pasamos al modelo
        if (carrito != null && !carrito.isEmpty()) {
            model.addAttribute("itemsCarrito", carrito);
        }
        
        model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(
            com.sivil.systeam.enums.Estado.activo, 0));
        return "compra-online/crear-compra";
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

    // Procesar datos del formulario y almacenar en sesión (NO crear en BD aún)
    @PostMapping("/crear")
    public String procesarFormularioCompraOnline(
            @RequestParam("nombreCliente") String nombreCliente,
            @RequestParam("contactoCliente") String contactoCliente,
            @RequestParam("identificacionCliente") String identificacionCliente,
            @RequestParam("direccionEntrega") String direccionEntrega,
            @RequestParam("librosData") String librosDataJson,
            HttpSession session,
            Model model) {

        try {
            // 1. Convertir JSON de libros a lista de objetos
            List<LibroCompraRequest> detallesRequest = objectMapper.readValue(
                    librosDataJson,
                    new TypeReference<List<LibroCompraRequest>>() {}
            );

            if (detallesRequest.isEmpty()) {
                model.addAttribute("error", "Debe agregar al menos un libro para realizar la compra.");
                model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(
                    com.sivil.systeam.enums.Estado.activo, 0));
                model.addAttribute("avisoTemporal", true);
                return "compra-online/crear-compra";
            }

            // 2. Verificar stock disponible para todos los libros
            for (LibroCompraRequest detalleRequest : detallesRequest) {
                Optional<Libro> libroOpt = libroRepository.findById(detalleRequest.getId());
                if (libroOpt.isEmpty()) {
                    model.addAttribute("error", "Libro no encontrado: " + detalleRequest.getId());
                    model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(
                        com.sivil.systeam.enums.Estado.activo, 0));
                    model.addAttribute("avisoTemporal", true);
                    return "compra-online/crear-compra";
                }

                Libro libro = libroOpt.get();
                if (libro.getCantidad_stock() < detalleRequest.getCantidad()) {
                    model.addAttribute("error", "Stock insuficiente para: " + libro.getTitulo() +
                            ". Stock disponible: " + libro.getCantidad_stock() +
                            ". Cantidad solicitada: " + detalleRequest.getCantidad());
                    model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(
                        com.sivil.systeam.enums.Estado.activo, 0));
                    model.addAttribute("avisoTemporal", true);
                    return "compra-online/crear-compra";
                }
            }

            // 3. Calcular totales de la compra
            BigDecimal subtotalCompra = BigDecimal.ZERO;
            for (LibroCompraRequest detalleRequest : detallesRequest) {
                BigDecimal subtotalItem = detalleRequest.getPrecio().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()));
                subtotalCompra = subtotalCompra.add(subtotalItem);
            }

            BigDecimal impuestos = subtotalCompra.multiply(new BigDecimal("0.13"));
            BigDecimal totalCompra = subtotalCompra.add(impuestos);

            // 4. Obtener usuario actual del modelo (agregado por GlobalControllerAdvice)
            Usuario usuarioActual = (Usuario) model.getAttribute("currentUser");


            // 5. Crear CompraTemporalDTO y guardar en sesión
            CompraTemporalDTO compraTemporal = new CompraTemporalDTO();
            compraTemporal.setNumeroOrden(numeracionService.generarNumeroFactura(compraOnlineRepository));
            compraTemporal.setComprador(usuarioActual);
            compraTemporal.setNombreCliente(nombreCliente);
            compraTemporal.setContactoCliente(contactoCliente);
            compraTemporal.setIdentificacionCliente(identificacionCliente);
            compraTemporal.setDireccionEntrega(direccionEntrega);
            compraTemporal.setSubtotal(subtotalCompra);
            compraTemporal.setImpuestos(impuestos);
            compraTemporal.setTotal(totalCompra);
            compraTemporal.setMetodoPago(com.sivil.systeam.enums.MetodoPago.tarjeta);
            compraTemporal.setEstadoCompra(com.sivil.systeam.enums.EstadoCompra.pendiente);
            compraTemporal.setFechaCompra(LocalDateTime.now());

            // 5. Crear lista de detalles temporales
            List<CompraTemporalDTO.DetalleCompraTemporalDTO> detallesTemporal = new ArrayList<>();
            for (LibroCompraRequest detalleRequest : detallesRequest) {
                CompraTemporalDTO.DetalleCompraTemporalDTO detalleTemporal =
                    new CompraTemporalDTO.DetalleCompraTemporalDTO(
                        detalleRequest.getId(),
                        detalleRequest.getTitulo(),
                        detalleRequest.getCantidad(),
                        detalleRequest.getPrecio(),
                        detalleRequest.getPrecio().multiply(BigDecimal.valueOf(detalleRequest.getCantidad()))
                    );
                detallesTemporal.add(detalleTemporal);
            }
            compraTemporal.setDetallesCompra(detallesTemporal);

            // 6. Guardar en sesión
            session.setAttribute("compraPendiente", compraTemporal);
            
            // 7. Limpiar carrito de la sesión después de procesar
            session.removeAttribute("carrito");

            // 8. Redirigir a pago con tarjeta
            return "redirect:/pago/tarjeta?monto=" + totalCompra + "&compraPendiente=true";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar la compra: " + e.getMessage());
            model.addAttribute("libros", libroRepository.findByEstadoAndCantidad_stockGreaterThan(
                com.sivil.systeam.enums.Estado.activo, 0));
            model.addAttribute("avisoTemporal", true);
            return "compra-online/crear-compra";
        }
    }

    // Endpoint para obtener historial de compras del usuario
    @GetMapping("/mis-compras")
    public String mostrarMisCompras(Model model) {
        // Obtener usuario actual del modelo (agregado por GlobalControllerAdvice)
        Usuario usuarioActual = (Usuario) model.getAttribute("currentUser");
        if (usuarioActual == null) {
            return "redirect:/login";
        }

        List<CompraOnline> compras = compraOnlineRepository.findByCompradorIdOrderByFechaDesc(usuarioActual.getId_usuario());
        model.addAttribute("compras", compras);
        return "compra-online/mis-compras";
    }

    // Clase interna para mapear el JSON del frontend
    public static class LibroCompraRequest {
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