package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controlador principal para el manejo del inventario de libros
 * HU013: Agregar Libro al Inventario
 * HU014: Actualizar Información de Libro
 *
 * Rutas principales:
 * - GET /stock
 * - GET /libro(s)/nuevo
 * - POST /libro(s)
 * - GET /libro(s)/{id}/editar
 * - POST /libro(s)/{id}
 */
@Controller
@RequestMapping
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    // ============================================================
    // INVENTARIO (LISTADO)
    // ============================================================
    @GetMapping("/stock")
    public String mostrarInventario(
            @RequestParam(value = "q", required = false) String q,
            Model model
    ) {
        try {
            List<Libro> libros;

            if (q != null && !q.trim().isEmpty()) {
                String termino = q.trim();

                // 1) Intento por CÓDIGO exacto
                Libro porCodigo = inventarioService.buscarPorCodigoLibro(termino);
                if (porCodigo != null) {
                    libros = List.of(porCodigo);
                } else {
                    // 2) Por TÍTULO (mín. 3 caracteres; insensible a mayúsculas)
                    if (termino.length() < 3) {
                        model.addAttribute("error", "Ingresa al menos 3 caracteres para buscar por título");
                        libros = inventarioService.obtenerLibrosActivos();
                    } else {
                        libros = inventarioService.buscarPorTitulo(termino)
                                .stream()
                                .sorted(java.util.Comparator.comparing(
                                        Libro::getTitulo, String.CASE_INSENSITIVE_ORDER))
                                .limit(20) // máximo 20 resultados
                                .toList();

                        if (libros.isEmpty()) {
                            model.addAttribute("error", "Libro no encontrado");
                        }
                    }
                }
                model.addAttribute("q", termino); // para re-mostrar el término en la vista
            } else {
                // Sin búsqueda: listado normal
                libros = inventarioService.obtenerTodosLosLibros();
            }

            model.addAttribute("libros", libros);
            model.addAttribute("totalLibros", libros.size());

            long librosStockBajo = libros.stream().filter(Libro::tieneStockBajo).count();
            model.addAttribute("librosStockBajo", librosStockBajo);

            return "stock/inventario";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el inventario: " + e.getMessage());
            return "stock/inventario";
        }
    }


    // ============================================================
    // HU013: NUEVO LIBRO
    // ============================================================
    @GetMapping({"/libro/nuevo", "/libros/nuevo"})
    public String nuevoLibro(Model model) {
        model.addAttribute("form", new Libro());
        return "libro/nuevo";
    }

    @PostMapping({"/libro", "/libros"})
    public String guardarLibro(@Valid @ModelAttribute("form") Libro libro,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirect) {
        try {
            if (result.hasErrors()) {
                model.addAttribute("form", libro);
                return "libro/nuevo";
            }

            // Validación: código único
            if (inventarioService.existePorCodigoLibro(libro.getCodigo_libro())) {
                model.addAttribute("error",
                        "El código del libro '" + libro.getCodigo_libro() + "' ya existe en el sistema");
                model.addAttribute("form", libro);
                return "libro/nuevo";
            }

            Libro guardado = inventarioService.guardarLibro(libro);
            redirect.addFlashAttribute("ok",
                    "Libro agregado exitosamente con código: " + guardado.getCodigo_libro());
            return "redirect:/stock";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", libro);
            return "libro/nuevo";
        } catch (Exception e) {
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            model.addAttribute("form", libro);
            return "libro/nuevo";
        }
    }

    // ============================================================
    // HU014: EDITAR / ACTUALIZAR LIBRO
    // ============================================================
    // Formulario de edición
    @GetMapping({"/libro/{id}/editar", "/libros/{id}/editar"})
    public String editarLibro(@PathVariable("id") Integer id,
                              Model model,
                              RedirectAttributes redirect) {
        try {
            Libro libro = inventarioService.obtenerLibroPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado"));
            model.addAttribute("form", libro);
            return "libro/editar";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/stock";
        }
    }

    // Procesar actualización
    @PostMapping({"/libro/{id}", "/libros/{id}"})
    public String actualizarLibro(@PathVariable("id") Integer id,
                                  @Valid @ModelAttribute("form") Libro form,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirect,
                                  Principal principal) {
        if (result.hasErrors()) {
            model.addAttribute("form", form);
            return "libro/editar";
        }

        String username = (principal != null ? principal.getName() : "sistema");

        try {
            Libro actualizado = inventarioService.actualizarLibro(id, form, username);
            redirect.addFlashAttribute("ok",
                    "Libro actualizado correctamente: " + actualizado.getCodigo_libro());
            return "redirect:/stock";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            return "libro/editar";
        } catch (Exception e) {
            model.addAttribute("error", "Error interno: " + e.getMessage());
            model.addAttribute("form", form);
            return "libro/editar";
        }
    }

    // ============================================================
    // DETALLE / ELIMINACIÓN
    // ============================================================
    @GetMapping({"/libro/{id}", "/libros/{id}"})
    public String verLibro(@PathVariable("id") Integer id, Model model) {
        try {
            // TODO: Vista de detalles
            return "redirect:/stock";
        } catch (Exception e) {
            return "redirect:/stock";
        }
    }

    @GetMapping({"/libro/{id}/eliminar", "/libros/{id}/eliminar"})
    public String mostrarConfirmacionEliminacion(@PathVariable("id") Integer id,
                                                 Model model,
                                                 RedirectAttributes redirect) {
        try {
            var libroOpt = inventarioService.obtenerLibroPorId(id);
            if (libroOpt.isEmpty()) {
                redirect.addFlashAttribute("error", "Libro no encontrado");
                return "redirect:/stock";
            }

            Libro libro = libroOpt.get();

            if (libro.getEstado() != com.sivil.systeam.enums.Estado.activo) {
                redirect.addFlashAttribute("error", "No se puede eliminar: el libro no está activo");
                return "redirect:/stock";
            }

            if (libro.getCantidad_stock() != null && libro.getCantidad_stock() > 0) {
                redirect.addFlashAttribute("error",
                        "No se puede eliminar: tiene " + libro.getCantidad_stock() + " unidades en stock");
                return "redirect:/stock";
            }

            model.addAttribute("libro", libro);
            return "libro/confirmar-eliminacion";

        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error del sistema: " + e.getMessage());
            return "redirect:/stock";
        }
    }
    // Confirmar eliminación (acepta plural/singular y con/sin "/confirmar")
    @PostMapping({
            "/libro/{id}/eliminar",
            "/libros/{id}/eliminar",
            "/libro/{id}/eliminar/confirmar",
            "/libros/{id}/eliminar/confirmar"
    })
    public String confirmarEliminacionLibro(@PathVariable("id") Integer id,
                                            RedirectAttributes redirect) {
        try {
            inventarioService.eliminarLibro(id);
            redirect.addFlashAttribute("ok", "Libro eliminado correctamente");
            return "redirect:/stock";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/stock";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error del sistema: " + e.getMessage());
            return "redirect:/stock";
        }
    }

    // ============================================================
    // ENDPOINTS AJAX
    // ============================================================
    @GetMapping("/api/libros/verificar-codigo")
    @ResponseBody
    public boolean verificarCodigoDisponible(@RequestParam("codigo") String codigo) {
        try {
            return !inventarioService.existePorCodigoLibro(codigo);
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/api/libros/buscar")
    @ResponseBody
    public List<Libro> buscarLibros(@RequestParam(required = false) String termino,
                                    @RequestParam(required = false) String categoria,
                                    @RequestParam(required = false) String autor) {
        try {
            if (categoria != null && !categoria.trim().isEmpty()) {
                return inventarioService.obtenerLibrosPorCategoria(categoria);
            } else if (autor != null && !autor.trim().isEmpty()) {
                return inventarioService.buscarPorAutor(autor);
            } else if (termino != null && !termino.trim().isEmpty()) {
                return inventarioService.buscarPorTitulo(termino);
            } else {
                return inventarioService.obtenerLibrosActivos();
            }
        } catch (Exception e) {
            return List.of();
        }
    }

    // ============================================================
    // MANEJO GLOBAL DE ERRORES EN ESTE CONTROLADOR
    // ============================================================
    @ExceptionHandler(Exception.class)
    public String manejarError(Exception e, Model model) {
        System.err.println("Error en InventarioController: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("error", "Ha ocurrido un error inesperado: " + e.getMessage());
        return "error";
    }
}
