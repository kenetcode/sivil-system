package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.enums.Estado;
import com.sivil.systeam.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador principal para el manejo del inventario de libros
 * Implementa HU013: Agregar Libro al Inventario
 *
 * Rutas principales:
 * - GET /stock → Lista de inventario
 * - GET /libros/nuevo → Formulario agregar libro
 * - POST /libros → Procesar nuevo libro (HU013)
 */
@Controller // Indica que es un controlador Spring MVC para páginas web
@RequestMapping // Sin valor = mapea desde la raíz "/"
public class InventarioController {

    // Inyección de dependencia del servicio que maneja la lógica de negocio de libros
    @Autowired
    private LibroService libroService;

    /**
     * MOSTRAR PÁGINA PRINCIPAL DEL INVENTARIO
     * Mapea: GET /stock
     * Vista: templates/stock/inventario.html
     *
     * Esta página muestra todos los libros activos en una tabla
     * Incluye estadísticas básicas del inventario
     */
    @GetMapping("/stock")
    public String mostrarInventario(Model model) {
        try {
            // Obtener todos los libros con estado "activo" desde la base de datos
            // El service se conecta al repository que hace la consulta SQL
            List<Libro> libros = libroService.obtenerTodosLosLibros();

            // Agregar la lista de libros al modelo para que Thymeleaf pueda acceder
            // En la vista HTML usaremos: th:each="l : ${libros}"
            model.addAttribute("libros", libros);

            // Calcular estadísticas básicas para mostrar en cards o badges
            model.addAttribute("totalLibros", libros.size()); // Total de libros activos

            // Contar libros con stock bajo usando el método tieneStockBajo() de la entidad
            // Stream API de Java para filtrar y contar elementos
            long librosStockBajo = libros.stream()
                    .filter(Libro::tieneStockBajo) // Filtrar solo libros con stock < 5
                    .count(); // Contar cuántos cumplen la condición
            model.addAttribute("librosStockBajo", librosStockBajo);

            // Retornar nombre de la vista (archivo templates/stock/inventario.html)
            return "stock/inventario";

        } catch (Exception e) {
            // Si hay error al conectar con BD o cualquier otro problema
            // Agregar mensaje de error para mostrar en la vista
            model.addAttribute("error", "Error al cargar el inventario: " + e.getMessage());
            return "stock/inventario"; // Mostrar la página con mensaje de error
        }
    }

    /**
     * HU013: MOSTRAR FORMULARIO PARA AGREGAR NUEVO LIBRO
     * Mapea: GET /libros/nuevo
     * Vista: templates/Libros/nuevo.html
     *
     * Prepara el formulario con un objeto Libro vacío para que Thymeleaf
     * pueda hacer binding de los campos del formulario HTML
     */
    @GetMapping("/libros/nuevo")
    public String nuevoLibro(Model model) {
        // Crear instancia vacía de Libro para binding con el formulario
        // Thymeleaf usará: th:object="${form}" y th:field="*{codigo_libro}"
        model.addAttribute("form", new Libro());

        // Retornar vista del formulario (templates/Libros/nuevo.html)
        return "Libros/nuevo";
    }

    /**
     * HU013: PROCESAR FORMULARIO DE NUEVO LIBRO - IMPLEMENTACIÓN COMPLETA
     * Mapea: POST /libros
     *
     * Esta es la funcionalidad CORE de la historia de usuario HU013
     * Implementa todos los criterios de aceptación:
     * 1. Validación de Código Único
     * 2. Registro Exitoso
     * 3. Campos Obligatorios
     * 4. Disponibilidad Inmediata
     *
     * @param libro - Objeto Libro con datos del formulario (automático binding)
     * @param result - Contiene errores de validación de las anotaciones @Valid
     * @param model - Para enviar datos a la vista en caso de error
     * @param redirectAttributes - Para mensajes flash al redirigir (éxito)
     */
    @PostMapping("/libros")
    public String guardarLibro(@Valid @ModelAttribute("form") Libro libro,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        try {
            // CRITERIO DE ACEPTACIÓN: CAMPOS OBLIGATORIOS
            // Verificar errores de validación de las anotaciones en la entidad
            // (@NotBlank, @NotNull, @Min, @Max, @Size, @Pattern)
            if (result.hasErrors()) {
                // Si hay errores, regresar al formulario manteniendo los datos ingresados
                model.addAttribute("form", libro); // Mantener datos del usuario
                // Spring automáticamente pasará los errores a la vista
                // Thymeleaf los mostrará con: th:errors="*{campo}"
                return "Libros/nuevo"; // Regresar al formulario con errores visibles
            }

            // CRITERIO DE ACEPTACIÓN: VALIDACIÓN DE CÓDIGO ÚNICO
            // Verificar que no exista otro libro con el mismo código en la BD
            if (libroService.existePorCodigoLibro(libro.getCodigo_libro())) {
                // Si el código ya existe, mostrar mensaje de error específico
                // Este es un error de negocio, no de validación de campo
                model.addAttribute("error",
                        "El código del libro '" + libro.getCodigo_libro() +
                                "' ya existe en el sistema");
                model.addAttribute("form", libro); // Mantener otros datos del formulario
                return "Libros/nuevo"; // Volver al formulario con error de código duplicado
            }

            // CRITERIO DE ACEPTACIÓN: REGISTRO EXITOSO
            // Si llegamos aquí: campos válidos + código único = guardar libro
            Libro libroGuardado = libroService.guardarLibro(libro);
            // El service establecerá estado=activo y validará reglas de negocio

            // CRITERIO DE ACEPTACIÓN: MENSAJE DE CONFIRMACIÓN CON CÓDIGO GENERADO
            // Flash message que se mostrará después del redirect
            redirectAttributes.addFlashAttribute("ok",
                    "Libro agregado exitosamente al inventario con código: " +
                            libroGuardado.getCodigo_libro());

            // CRITERIO DE ACEPTACIÓN: DISPONIBILIDAD INMEDIATA
            // Redirigir al inventario donde el libro aparecerá inmediatamente
            // El stock inicial se reflejará correctamente en la tabla
            return "redirect:/stock";

        } catch (IllegalArgumentException e) {
            // Errores de validación de reglas de negocio desde el service:
            // - Año de publicación inválido (< 1900 o > año actual)
            // - Precio negativo o cero
            // - Stock negativo
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", libro); // Mantener datos del formulario
            return "Libros/nuevo"; // Volver al formulario con error específico

        } catch (Exception e) {
            // Cualquier otro error no previsto (BD desconectada, etc.)
            model.addAttribute("error", "Error interno del servidor: " + e.getMessage());
            model.addAttribute("form", libro);
            return "Libros/nuevo";
        }
    }

    /**
     * VER DETALLES DE UN LIBRO ESPECÍFICO
     * Mapea: GET /libros/{id}
     * Para futuras funcionalidades (HU016: Consultar Inventario detallado)
     * Por ahora redirige al inventario principal
     */
    @GetMapping("/libros/{id}")
    public String verLibro(@PathVariable Integer id, Model model) {
        try {
            // TODO: Implementar vista de detalles de libro individual
            // Buscar libro por ID y mostrar información completa
            return "redirect:/stock"; // Por ahora redirigir al inventario
        } catch (Exception e) {
            return "redirect:/stock";
        }
    }

    /**
     * MOSTRAR FORMULARIO DE EDICIÓN DE LIBRO
     * Mapea: GET /libros/{id}/editar
     * Para futuras funcionalidades (HU014: Actualizar Información de Libro)
     * Por ahora redirige al inventario principal
     */
    @GetMapping("/libros/{id}/editar")
    public String editarLibro(@PathVariable Integer id, Model model) {
        try {
            // TODO: Implementar HU014 - formulario de edición
            // 1. Buscar libro por ID
            // 2. Cargar datos en formulario
            // 3. Permitir editar (excepto código si ya tiene ventas)
            return "redirect:/stock";
        } catch (Exception e) {
            return "redirect:/stock";
        }
    }

    /**
     * ENDPOINT AJAX: VERIFICAR DISPONIBILIDAD DE CÓDIGO DE LIBRO
     * Mapea: GET /api/libros/verificar-codigo
     * Responde: JSON boolean
     *
     * Usado por JavaScript para validación en tiempo real mientras el usuario escribe
     * Mejora la experiencia de usuario mostrando si el código está disponible
     * sin necesidad de enviar el formulario completo
     *
     * @param codigo - Código del libro a verificar (parámetro ?codigo=ABC123)
     * @return boolean - true si está disponible, false si ya existe
     */
    @GetMapping("/api/libros/verificar-codigo")
    @ResponseBody // Indica que la respuesta es JSON, no una vista HTML
    public boolean verificarCodigoDisponible(@RequestParam String codigo) {
        try {
            // Consultar si existe un libro con ese código
            // Retornar true si NO existe (está disponible), false si ya existe
            return !libroService.existePorCodigoLibro(codigo);
        } catch (Exception e) {
            // En caso de error (BD desconectada, etc.), asumir no disponible
            // Es más seguro fallar hacia el lado de "no disponible"
            return false;
        }
    }

    /**
     * ENDPOINT AJAX: BUSCAR LIBROS POR TÉRMINO
     * Mapea: GET /api/libros/buscar
     * Responde: JSON List<Libro>
     *
     * Para implementar búsquedas dinámicas en el frontend
     * Permite filtrar libros por categoría, autor o término general
     */
    @GetMapping("/api/libros/buscar")
    @ResponseBody // Respuesta JSON
    public List<Libro> buscarLibros(@RequestParam(required = false) String termino,
                                    @RequestParam(required = false) String categoria,
                                    @RequestParam(required = false) String autor) {
        try {
            // Búsqueda por diferentes criterios según parámetros recibidos
            if (categoria != null && !categoria.trim().isEmpty()) {
                return libroService.obtenerLibrosPorCategoria(categoria);
            } else if (autor != null && !autor.trim().isEmpty()) {
                return libroService.buscarPorAutor(autor);
            } else {
                // Por defecto, retornar todos los libros activos
                return libroService.obtenerLibrosActivos();
            }
        } catch (Exception e) {
            // En caso de error, retornar lista vacía en lugar de excepción
            return List.of(); // Lista inmutable vacía
        }
    }

    /**
     * MOSTRAR LIBROS CON STOCK BAJO
     * Mapea: GET /stock/bajo
     * Vista: templates/stock/inventario.html (misma vista, datos filtrados)
     *
     * Funcionalidad adicional para alertas de reabastecimiento
     * Muestra solo libros con menos de 5 unidades en stock
     */
    @GetMapping("/stock/bajo")
    public String librosStockBajo(Model model) {
        try {
            // Obtener libros con stock crítico (menos de 5 unidades)
            List<Libro> librosStockBajo = libroService.obtenerLibrosStockBajo();

            // Usar la misma plantilla HTML pero con datos filtrados
            model.addAttribute("libros", librosStockBajo);

            // Cambiar títulos para indicar que es una vista filtrada
            model.addAttribute("titulo", "Libros con Stock Bajo");
            model.addAttribute("descripcion",
                    "Libros que necesitan reabastecimiento (menos de 5 unidades)");

            // Misma vista HTML que el inventario principal
            return "stock/inventario";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar libros con stock bajo");
            return "redirect:/stock"; // En caso de error, ir al inventario principal
        }
    }

    /**
     * MOSTRAR ESTADÍSTICAS DEL INVENTARIO
     * Mapea: GET /stock/estadisticas
     * Vista: templates/stock/estadisticas.html
     *
     * Dashboard con métricas importantes del inventario:
     * - Total de libros, total en stock, valor del inventario
     * - Libros con stock bajo, libros sin stock
     * - Gráficos y reportes (implementar vista si es necesario)
     */
    @GetMapping("/stock/estadisticas")
    public String estadisticasInventario(Model model) {
        try {
            // Obtener estadísticas calculadas desde el servicio
            // El service hace consultas agregadas a la base de datos
            var estadisticas = libroService.obtenerEstadisticasInventario();
            model.addAttribute("estadisticas", estadisticas);

            // Vista específica para estadísticas
            // TODO: Crear templates/stock/estadisticas.html si se necesita
            return "stock/estadisticas";

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar estadísticas");
            return "redirect:/stock"; // En caso de error, regresar al inventario
        }
    }

    /**
     * MANEJADOR GLOBAL DE ERRORES PARA ESTE CONTROLADOR
     * Se ejecuta cuando hay una excepción no controlada en cualquier método de este controlador
     *
     * @param e - Excepción que ocurrió
     * @param model - Para pasar mensaje de error a la vista
     * @return Vista de error genérica
     */
    @ExceptionHandler(Exception.class)
    public String manejarError(Exception e, Model model) {
        // Log del error en consola para debugging (en producción usar logger)
        System.err.println("Error en InventarioController: " + e.getMessage());
        e.printStackTrace(); // Stack trace completo para debugging

        // Mostrar página de error genérica con mensaje amigable al usuario
        model.addAttribute("error", "Ha ocurrido un error inesperado: " + e.getMessage());

        // Retornar vista de error general
        // TODO: Crear templates/error.html con diseño consistente
        return "error";
    }
}