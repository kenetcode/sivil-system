package com.sivil.systeam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador para manejar las vistas web con Thymeleaf
 * Nota: Este es un @Controller (NO @RestController) para devolver nombres de templates
 */
@Controller
public class ViewController {

    /**
     * Página principal del sistema
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Vista de lista de libros
     */
    @GetMapping("/libros")
    public String listaLibros() {
        return "libro/lista";
    }

    /**
     * Vista de libros activos
     */
    @GetMapping("/libros/activos")
    public String librosActivos() {
        return "libro/lista";
    }

    /**
     * Vista de libros disponibles (con stock)
     */
    @GetMapping("/libros/disponibles")
    public String librosDisponibles() {
        return "libro/lista";
    }

    /**
     * Vista de detalle de un libro específico
     */
    @GetMapping("/libros/detalle")
    public String detalleLibro(@RequestParam(required = false) Integer id) {
        return "libro/detalle";
    }

    /**
     * Vista de búsqueda avanzada de libros
     */
    @GetMapping("/libros/buscar")
    public String buscarLibros() {
        return "libro/buscar";
    }

    /**
     * Vista de libros por categoría
     */
    @GetMapping("/libros/categoria")
    public String librosPorCategoria(@RequestParam String categoria) {
        return "libro/lista";
    }
}