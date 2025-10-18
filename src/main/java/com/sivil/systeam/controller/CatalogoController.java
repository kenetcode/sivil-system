package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para el catálogo de libros online
 * Maneja la visualización, búsqueda y selección de libros para compra
 */
@Controller
@RequestMapping("/")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    /**
     * Muestra el catálogo de libros en la página principal
     */
    @GetMapping
    public String mostrarCatalogo(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "autor", required = false) String autor,
            @RequestParam(value = "orden", required = false, defaultValue = "titulo-asc") String orden,
            @RequestParam(value = "pagina", required = false, defaultValue = "0") int pagina,
            Model model,
            HttpSession session) {
        
        // Obtener libros con filtros y paginación
        Page<Libro> paginaLibros = catalogoService.buscarYFiltrarLibros(busqueda, autor, orden, pagina);
        
        // Obtener autores disponibles para el filtro
        List<String> autores = catalogoService.obtenerAutoresDisponibles();
        
        // Obtener carrito de la sesión
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        
        // Calcular total de items en el carrito
        int totalItemsCarrito = carrito.stream()
                .mapToInt(ItemCarrito::getCantidad)
                .sum();
        
        // Agregar atributos al modelo
        model.addAttribute("libros", paginaLibros.getContent());
        model.addAttribute("paginaActual", pagina);
        model.addAttribute("totalPaginas", paginaLibros.getTotalPages());
        model.addAttribute("totalLibros", paginaLibros.getTotalElements());
        model.addAttribute("autores", autores);
        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("autorSeleccionado", autor != null ? autor : "todos");
        model.addAttribute("ordenSeleccionado", orden);
        model.addAttribute("totalItemsCarrito", totalItemsCarrito);
        
        return "index";
    }

    /**
     * API REST para obtener detalles completos de un libro
     */
    @GetMapping("/api/libro/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDetallesLibro(@PathVariable Integer id) {
        Libro libro = catalogoService.obtenerLibroDisponiblePorId(id);
        
        if (libro == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> detalles = new HashMap<>();
        detalles.put("id", libro.getId_libro());
        detalles.put("codigo", libro.getCodigo_libro());
        detalles.put("titulo", libro.getTitulo());
        detalles.put("autor", libro.getAutor());
        detalles.put("precio", libro.getPrecio());
        detalles.put("stock", libro.getCantidad_stock());
        detalles.put("categoria", libro.getCategoria());
        detalles.put("editorial", libro.getEditorial());
        detalles.put("año", libro.getAño_publicacion());
        detalles.put("descripcion", libro.getDescripcion());
        detalles.put("imagenUrl", libro.getImagen_url());
        
        return ResponseEntity.ok(detalles);
    }

    /**
     * Agregar libro al carrito de compras
     */
    @PostMapping("/catalogo/agregar-carrito")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAlCarrito(
            @RequestParam Integer libroId,
            @RequestParam Integer cantidad,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verificar que el libro existe y está disponible
            Libro libro = catalogoService.obtenerLibroDisponiblePorId(libroId);
            if (libro == null) {
                response.put("success", false);
                response.put("mensaje", "El libro no está disponible");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Verificar stock suficiente
            if (libro.getCantidad_stock() < cantidad) {
                response.put("success", false);
                response.put("mensaje", "Stock insuficiente. Disponible: " + libro.getCantidad_stock());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Obtener o crear carrito en sesión
            @SuppressWarnings("unchecked")
            List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
            if (carrito == null) {
                carrito = new ArrayList<>();
            }
            
            // Verificar si el libro ya está en el carrito
            ItemCarrito itemExistente = carrito.stream()
                    .filter(item -> item.getLibroId().equals(libroId))
                    .findFirst()
                    .orElse(null);
            
            if (itemExistente != null) {
                // Actualizar cantidad
                int nuevaCantidad = itemExistente.getCantidad() + cantidad;
                if (libro.getCantidad_stock() < nuevaCantidad) {
                    response.put("success", false);
                    response.put("mensaje", "Stock insuficiente para esta cantidad total. Disponible: " + libro.getCantidad_stock());
                    return ResponseEntity.badRequest().body(response);
                }
                itemExistente.setCantidad(nuevaCantidad);
            } else {
                // Agregar nuevo item
                ItemCarrito nuevoItem = new ItemCarrito(
                        libro.getId_libro(),
                        libro.getTitulo(),
                        libro.getPrecio(),
                        cantidad
                );
                carrito.add(nuevoItem);
            }
            
            // Guardar carrito en sesión
            session.setAttribute("carrito", carrito);
            
            // Calcular total de items
            int totalItems = carrito.stream()
                    .mapToInt(ItemCarrito::getCantidad)
                    .sum();
            
            response.put("success", true);
            response.put("mensaje", "Libro agregado al carrito exitosamente");
            response.put("totalItemsCarrito", totalItems);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("mensaje", "Error al agregar al carrito: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Ver el carrito de compras
     */
    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
        }
        
        model.addAttribute("carrito", carrito);
        return "Compra/carrito";
    }

    /**
     * Eliminar item del carrito
     */
    @PostMapping("/carrito/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDelCarrito(
            @RequestParam Integer libroId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito != null) {
            carrito.removeIf(item -> item.getLibroId().equals(libroId));
            session.setAttribute("carrito", carrito);
            
            int totalItems = carrito.stream()
                    .mapToInt(ItemCarrito::getCantidad)
                    .sum();
            
            response.put("success", true);
            response.put("totalItemsCarrito", totalItems);
        } else {
            response.put("success", false);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar cantidad de un item en el carrito
     */
    @PostMapping("/carrito/actualizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarCantidadCarrito(
            @RequestParam Integer libroId,
            @RequestParam Integer cantidad,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        List<ItemCarrito> carrito = (List<ItemCarrito>) session.getAttribute("carrito");
        if (carrito != null) {
            ItemCarrito item = carrito.stream()
                    .filter(i -> i.getLibroId().equals(libroId))
                    .findFirst()
                    .orElse(null);
            
            if (item != null) {
                // Verificar stock
                Libro libro = catalogoService.obtenerLibroDisponiblePorId(libroId);
                if (libro != null && libro.getCantidad_stock() >= cantidad) {
                    item.setCantidad(cantidad);
                    session.setAttribute("carrito", carrito);
                    response.put("success", true);
                } else {
                    response.put("success", false);
                    response.put("mensaje", "Stock insuficiente");
                }
            }
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Limpiar carrito
     */
    @PostMapping("/carrito/limpiar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> limpiarCarrito(HttpSession session) {
        // Eliminar el carrito de la sesión
        session.removeAttribute("carrito");
        
        // Crear un nuevo carrito vacío para asegurar el estado limpio
        session.setAttribute("carrito", new ArrayList<ItemCarrito>());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalItemsCarrito", 0);
        return ResponseEntity.ok(response);
    }

    /**
     * Clase interna para representar items del carrito en sesión
     */
    public static class ItemCarrito implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer libroId;
        private String titulo;
        private java.math.BigDecimal precio;
        private Integer cantidad;

        public ItemCarrito() {}

        public ItemCarrito(Integer libroId, String titulo, java.math.BigDecimal precio, Integer cantidad) {
            this.libroId = libroId;
            this.titulo = titulo;
            this.precio = precio;
            this.cantidad = cantidad;
        }

        // Getters y Setters
        public Integer getLibroId() { return libroId; }
        public void setLibroId(Integer libroId) { this.libroId = libroId; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public java.math.BigDecimal getPrecio() { return precio; }
        public void setPrecio(java.math.BigDecimal precio) { this.precio = precio; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public java.math.BigDecimal getSubtotal() {
            return precio.multiply(java.math.BigDecimal.valueOf(cantidad));
        }
    }
}
