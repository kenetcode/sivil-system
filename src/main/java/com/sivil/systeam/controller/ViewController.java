package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

/**
 * Controlador para manejar las vistas web con Thymeleaf
 * Nota: Este es un @Controller (NO @RestController) para devolver nombres de templates
 */
@Controller
public class ViewController {

    @Autowired
    private InventarioService inventarioService;

    /**
     * Página principal del sistema
     * NOTA: Esta ruta ahora es manejada por CatalogoController para mostrar el catálogo de libros
     * Mantenida comentada para referencia
     */
    /*
    @GetMapping("/")
    public String index(Model model) {
        // Obtener todos los libros y pasarlos al modelo
        List<Libro> libros = inventarioService.obtenerLibrosActivos();
        model.addAttribute("libros", libros);
        return "index";
    }
    */

    //Datos de ejemplo para la tabla, solo de ejemplo, usaremos endpoints REST para datos reales
    //http://localhost:8080/tabla-ejemplo
    //solo es un ejemplo de como usar la tabla con Thymeleaf, no sera parte de la forma de crear tablas en el proyecto
    //la idea es usar endpoints REST para obtener los datos y mostrarlos en la tabla
    //la tabla es un componente reutilizable, se puede usar en cualquier vista
    //la tabla recibe los datos y la configuracion de las columnas desde el modelo
    //la tabla tiene botones de acciones (ver, editar, eliminar) que redirigen a las vistas correspondientes
    //la tabla tiene un boton de agregar que redirige a la vista de agregar
    @GetMapping("/tabla-ejemplo")
    public String tablaEjemplo(Model model) {
        // Datos de prueba
        List<Map<String, Object>> items = Arrays.asList(
                Map.of("id", 1, "codigo", "LIB001", "titulo", "El Quijote", "autor", "Cervantes", "precio", 25000, "stock", 5, "categoria", "Clásicos"),
                Map.of("id", 2, "codigo", "LIB002", "titulo", "1984", "autor", "Orwell", "precio", 28000, "stock", 0, "categoria", "Ciencia Ficción")
        );
        
        // Configuración de columnas
        List<Map<String, Object>> columns = Arrays.asList(
                Map.of("label", "Código", "getter", "codigo", "type", "code"),
                Map.of("label", "Título", "getter", "titulo", "type", "strong"),
                Map.of("label", "Autor", "getter", "autor", "type", "text"),
                Map.of("label", "Precio", "getter", "precio", "type", "price"),
                Map.of("label", "Stock", "getter", "stock", "type", "conditional-badge"),
                Map.of("label", "Categoría", "getter", "categoria", "type", "badge", "badgeClass", "bg-info")
        );

        model.addAttribute("title", "Título");
        model.addAttribute("subtitle", "Agregar libros al catalogo");
        model.addAttribute("items", items);
        model.addAttribute("columns", columns);
        model.addAttribute("showActions", true);
        model.addAttribute("showAddButton", true);
        model.addAttribute("showViewButton", true);
        model.addAttribute("showEditButton", true);
        model.addAttribute("showDeleteButton", true);
        
        return "tabla-ejemplo";
    }

}