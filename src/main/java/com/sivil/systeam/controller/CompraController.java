package com.sivil.systeam.controller;

import com.sivil.systeam.entity.CompraOnline;
import com.sivil.systeam.entity.DetalleCompra;
import com.sivil.systeam.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class CompraController {

    @Autowired
    private CompraService compraService;

    @GetMapping("/compra-online/{id}/editar")
    public String mostrarFormularioEditarCompra(@PathVariable("id") Integer id, Model model, RedirectAttributes ra) {
        Optional<CompraOnline> compraOpt = compraService.findCompraById(id);

        if (compraOpt.isEmpty()) {
            ra.addFlashAttribute("error", "La compra que intentas editar no existe.");
            return "redirect:/compra-online/mis-compras";
        }

        CompraOnline compra = compraOpt.get();

        model.addAttribute("compra", compra);
        return "compra-online/editar-compra";
    }

    @PostMapping("/compra-online/{id}/editar")
    public String procesarEdicionCompra(@PathVariable("id") Integer id,
                                        @RequestParam("direccionEntrega") String direccionEntrega,
                                        RedirectAttributes ra) {
        try {
            compraService.actualizarDireccionCompra(id, direccionEntrega);
            ra.addFlashAttribute("mensaje", "¡La dirección de la compra ha sido actualizada con éxito!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }

        return "redirect:/compra-online/mis-compras";
    }

    @GetMapping("/compra-online/test")
    @ResponseBody
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Endpoint funcionando correctamente");
    }

    @GetMapping("/compra-online/{id}/detalles")
    @ResponseBody
    public ResponseEntity<?> obtenerDetallesCompra(@PathVariable("id") Integer id) {
        System.out.println("DEBUG: Llamada al endpoint con ID: " + id);
        
        try {
            Optional<CompraOnline> compraOpt = compraService.findCompraById(id);
            System.out.println("DEBUG: Compra encontrada: " + compraOpt.isPresent());
            
            if (compraOpt.isEmpty()) {
                System.out.println("DEBUG: Compra no encontrada");
                return ResponseEntity.notFound().build();
            }

            CompraOnline compra = compraOpt.get();
            System.out.println("DEBUG: Compra ID: " + compra.getId_compra() + ", Numero: " + compra.getNumero_orden());
            
            List<DetalleCompra> detalles = compraService.obtenerDetallesCompra(id);
            System.out.println("DEBUG: Detalles encontrados: " + detalles.size());
            
            // Crear DTOs para evitar problemas de serialización
            CompraDetalleResponse response = new CompraDetalleResponse();
            
            // Mapear compra a DTO
            CompraDTO compraDTO = new CompraDTO();
            compraDTO.setId_compra(compra.getId_compra());
            compraDTO.setNumero_orden(compra.getNumero_orden());
            compraDTO.setSubtotal(compra.getSubtotal());
            compraDTO.setImpuestos(compra.getImpuestos());
            compraDTO.setTotal(compra.getTotal());
            compraDTO.setDireccion_entrega(compra.getDireccion_entrega());
            compraDTO.setEstado_compra(compra.getEstado_compra() != null ? compra.getEstado_compra().name() : null);
            compraDTO.setMetodo_pago(compra.getMetodo_pago() != null ? compra.getMetodo_pago().name() : null);
            compraDTO.setFecha_compra(compra.getFecha_compra());
            
            response.setCompra(compraDTO);
            
            // Mapear detalles a DTOs
            List<DetalleCompraDTO> detallesDTO = detalles.stream().map(detalle -> {
                DetalleCompraDTO dto = new DetalleCompraDTO();
                dto.setId_detalle_compra(detalle.getId_detalle_compra());
                dto.setCantidad(detalle.getCantidad());
                dto.setPrecio_unitario(detalle.getPrecio_unitario());
                dto.setSubtotal_item(detalle.getSubtotal_item());
                
                System.out.println("DEBUG: Procesando detalle ID: " + detalle.getId_detalle_compra());
                
                // Mapear información del libro
                if (detalle.getLibro() != null) {
                    LibroDTO libroDTO = new LibroDTO();
                    libroDTO.setId_libro(detalle.getLibro().getId_libro());
                    libroDTO.setTitulo(detalle.getLibro().getTitulo());
                    libroDTO.setAutor(detalle.getLibro().getAutor());
                    libroDTO.setPrecio(detalle.getLibro().getPrecio());
                    dto.setLibro(libroDTO);
                    System.out.println("DEBUG: Libro asociado: " + detalle.getLibro().getTitulo());
                } else {
                    System.out.println("DEBUG: Sin libro asociado para detalle: " + detalle.getId_detalle_compra());
                }
                
                return dto;
            }).toList();
            
            response.setDetalles(detallesDTO);
            
            System.out.println("DEBUG: Respuesta creada con " + detallesDTO.size() + " detalles");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("DEBUG: Error al obtener detalles: " + e.getMessage());
            e.printStackTrace(); // Para debug
            return ResponseEntity.badRequest().body("Error al obtener los detalles: " + e.getMessage());
        }
    }

    // Clases DTO para evitar problemas de serialización
    public static class CompraDetalleResponse {
        private CompraDTO compra;
        private List<DetalleCompraDTO> detalles;

        public CompraDTO getCompra() { return compra; }
        public void setCompra(CompraDTO compra) { this.compra = compra; }

        public List<DetalleCompraDTO> getDetalles() { return detalles; }
        public void setDetalles(List<DetalleCompraDTO> detalles) { this.detalles = detalles; }
    }
    
    public static class CompraDTO {
        private Integer id_compra;
        private String numero_orden;
        private BigDecimal subtotal;
        private BigDecimal impuestos;
        private BigDecimal total;
        private String direccion_entrega;
        private String estado_compra;
        private String metodo_pago;
        private LocalDateTime fecha_compra;

        // Getters y Setters
        public Integer getId_compra() { return id_compra; }
        public void setId_compra(Integer id_compra) { this.id_compra = id_compra; }

        public String getNumero_orden() { return numero_orden; }
        public void setNumero_orden(String numero_orden) { this.numero_orden = numero_orden; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getImpuestos() { return impuestos; }
        public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        public String getDireccion_entrega() { return direccion_entrega; }
        public void setDireccion_entrega(String direccion_entrega) { this.direccion_entrega = direccion_entrega; }

        public String getEstado_compra() { return estado_compra; }
        public void setEstado_compra(String estado_compra) { this.estado_compra = estado_compra; }

        public String getMetodo_pago() { return metodo_pago; }
        public void setMetodo_pago(String metodo_pago) { this.metodo_pago = metodo_pago; }

        public LocalDateTime getFecha_compra() { return fecha_compra; }
        public void setFecha_compra(LocalDateTime fecha_compra) { this.fecha_compra = fecha_compra; }
    }
    
    public static class DetalleCompraDTO {
        private Integer id_detalle_compra;
        private Integer cantidad;
        private BigDecimal precio_unitario;
        private BigDecimal subtotal_item;
        private LibroDTO libro;

        // Getters y Setters
        public Integer getId_detalle_compra() { return id_detalle_compra; }
        public void setId_detalle_compra(Integer id_detalle_compra) { this.id_detalle_compra = id_detalle_compra; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public BigDecimal getPrecio_unitario() { return precio_unitario; }
        public void setPrecio_unitario(BigDecimal precio_unitario) { this.precio_unitario = precio_unitario; }

        public BigDecimal getSubtotal_item() { return subtotal_item; }
        public void setSubtotal_item(BigDecimal subtotal_item) { this.subtotal_item = subtotal_item; }

        public LibroDTO getLibro() { return libro; }
        public void setLibro(LibroDTO libro) { this.libro = libro; }
    }
    
    public static class LibroDTO {
        private Integer id_libro;
        private String titulo;
        private String autor;
        private BigDecimal precio;

        // Getters y Setters
        public Integer getId_libro() { return id_libro; }
        public void setId_libro(Integer id_libro) { this.id_libro = id_libro; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getAutor() { return autor; }
        public void setAutor(String autor) { this.autor = autor; }

        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
    }




}
