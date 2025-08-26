package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para manejar las vistas web con Thymeleaf
 * Nota: Este es un @Controller (NO @RestController) para devolver nombres de templates
 */
@Controller
public class ViewController {

    @Autowired
    private LibroService libroService;

    /**
     * Página principal del sistema
     */
    @GetMapping("/")
    public String index(Model model) {
        // Obtener todos los libros y pasarlos al modelo
        List<Libro> libros = libroService.obtenerLibrosActivos();
        model.addAttribute("libros", libros);
        return "index";
    }

}