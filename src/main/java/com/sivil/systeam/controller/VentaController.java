package com.sivil.systeam.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sivil.systeam.entity.*;
import com.sivil.systeam.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // Mostrar el formulario para crear venta
    @GetMapping("/crear")
    public String mostrarFormularioCrearVenta(Model model) {
        model.addAttribute("venta", new Venta());
        model.addAttribute("libros", libroRepository.findAll());
        return "venta/crear-venta"; // Asegúrate que esta ruta coincide con tu template
    }

    // Recibir datos del formulario y procesar la venta
    @PostMapping("/crear")
    @Transactional
    public String procesarFormularioCrearVenta(
            @ModelAttribute("venta") Venta venta,
            @RequestParam("librosData") String librosDataJson,
            Model model) {

        try {
            // 1. Convertir JSON de libros a lista de objetos
            List<LibroVentaRequest> detallesRequest = objectMapper.readValue(
                    librosDataJson,
                    new TypeReference<List<LibroVentaRequest>>() {}
            );

            if (detallesRequest.isEmpty()) {
                model.addAttribute("error", "Debe agregar al menos un libro para realizar la venta.");
                model.addAttribute("libros", libroRepository.findAll());
                return "venta/crear-venta";
            }

            // 2. Obtener el usuario vendedor desde el modelo (agregado por GlobalControllerAdvice)
            Usuario vendedor = (Usuario) model.getAttribute("currentUser");

            if (vendedor == null) {
                model.addAttribute("error", "No se pudo identificar al vendedor. Por favor inicie sesión nuevamente.");
                model.addAttribute("libros", libroRepository.findAll());
                return "venta/crear-venta";
            }

            venta.setVendedor(vendedor);

            // 3. Verificar que el número de factura no exista
            if (ventaRepository.existsByNumero_Factura(venta.getNumero_factura())) {
                model.addAttribute("error", "El número de factura ya existe.");
                model.addAttribute("libros", libroRepository.findAll());
                return "venta/crear-venta";
            }

            // 4. Guardar la venta
            venta.setFecha_venta(LocalDateTime.now());
            venta.setEstado(com.sivil.systeam.enums.EstadoVenta.activa);
            Venta ventaGuardada = ventaRepository.save(venta);

            // 5. Procesar cada detalle de venta
            for (LibroVentaRequest detalleRequest : detallesRequest) {
                Optional<Libro> libroOpt = libroRepository.findById(detalleRequest.getId());

                if (libroOpt.isEmpty()) {
                    throw new RuntimeException("Libro no encontrado: " + detalleRequest.getId());
                }

                Libro libro = libroOpt.get();

                // Verificar stock suficiente
                if (libro.getCantidad_stock() < detalleRequest.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + libro.getTitulo() +
                            ". Stock disponible: " + libro.getCantidad_stock());
                }

                // Crear detalle de venta
                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(ventaGuardada);
                detalle.setLibro(libro);
                detalle.setCantidad(detalleRequest.getCantidad());
                detalle.setPrecio_unitario(detalleRequest.getPrecio());
                detalle.setSubtotal_item(detalleRequest.getPrecio().multiply(
                        BigDecimal.valueOf(detalleRequest.getCantidad())));

                detalleVentaRepository.save(detalle);

                // Actualizar stock del libro
                libro.setCantidad_stock(libro.getCantidad_stock() - detalleRequest.getCantidad());
                libroRepository.save(libro);
            }

            model.addAttribute("mensaje", "Venta creada exitosamente!");
            model.addAttribute("venta", new Venta()); // Limpia el formulario
            model.addAttribute("libros", libroRepository.findAll());
            return "venta/crear-venta";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al crear la venta: " + e.getMessage());
            model.addAttribute("libros", libroRepository.findAll());
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