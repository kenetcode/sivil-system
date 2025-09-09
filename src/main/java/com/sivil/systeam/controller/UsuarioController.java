package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.TipoUsuario;
import com.sivil.systeam.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Arrays;
import java.util.List;

@Controller
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/login")
    public String mostrarLogin(@RequestParam(required = false) String error, Model model) {
        if ("credenciales".equals(error)) {
            model.addAttribute("error", "Email o contraseña incorrectos");
        } else if ("sistema".equals(error)) {
            model.addAttribute("error", "Error en el sistema");
        }
        return "login/login";
    }
    
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        // Solo permitir comprador y vendedor en el registro público
        List<TipoUsuario> tiposPermitidos = Arrays.asList(TipoUsuario.comprador, TipoUsuario.vendedor);
        model.addAttribute("tiposUsuario", tiposPermitidos);
        return "usuario/registro";
    }
    
    @GetMapping("/validar-nombre-usuario")
    @ResponseBody
    public boolean validarNombreUsuario(@RequestParam String nombre_usuario) {
        return !usuarioService.existeNombreUsuario(nombre_usuario);
    }
    
    @GetMapping("/validar-email")
    @ResponseBody
    public boolean validarEmail(@RequestParam String email) {
        return !usuarioService.existeEmail(email);
    }
    
    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, 
                                   RedirectAttributes redirectAttributes) {
        try {
            usuarioService.crearUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado exitosamente");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro";
        }
    }
}