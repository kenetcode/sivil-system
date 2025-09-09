package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Venta;
import com.sivil.systeam.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private LibroRepository libroRepository;

    // Mostrar el formulario para crear venta
    @GetMapping("/crear")
    public String mostrarFormularioCrearVenta(Model model) {
        model.addAttribute("venta", new Venta()); // Objeto vacío para el formulario
        model.addAttribute("libros", libroRepository.findAll()); // Enviar libros al formulario
        return "venta/crear-venta"; // Nombre del template Thymeleaf
    }

    // Recibir datos del formulario
    @PostMapping("/crear")
    public String procesarFormularioCrearVenta(@ModelAttribute("venta") Venta venta) {
        System.out.println("Venta recibida: " + venta);
        return "venta/venta-confirmada";
    }
}
