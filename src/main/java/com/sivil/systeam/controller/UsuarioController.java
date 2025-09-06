package com.sivil.systeam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {
    
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error, Model model) {
        if ("credenciales".equals(error)) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        } else if ("sistema".equals(error)) {
            model.addAttribute("error", "Error en el sistema");
        }
        return "login/login";
    }
}