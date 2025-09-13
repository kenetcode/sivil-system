package com.sivil.systeam.config;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            
            if (usuario != null) {
                model.addAttribute("currentUser", usuario);
                model.addAttribute("isAdmin", usuario.getTipo_usuario().name().equalsIgnoreCase("ADMIN"));
            }
        }
    }
}