package com.sivil.systeam.service;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.TipoUsuario;
import com.sivil.systeam.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    public Usuario validarLogin(String email, String contraseña) {
        Optional<Usuario> usuario = usuarioRepository.findByEmailAndContraseña(email, contraseña);
        return usuario.orElse(null);
    }
    
    public Usuario getUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();
            return usuarioRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
    
    public boolean esAdministrador() {
        Usuario usuario = getUsuarioActual();
        return usuario != null && usuario.getTipo_usuario() == TipoUsuario.admin;
    }
    
    public TipoUsuario getTipoUsuarioActual() {
        Usuario usuario = getUsuarioActual();
        return usuario != null ? usuario.getTipo_usuario() : null;
    }
}