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
    
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
    
    public boolean existeNombreUsuario(String nombre_usuario) {
        return usuarioRepository.existsByNombreUsuario(nombre_usuario);
    }
    
    public Usuario crearUsuario(Usuario usuario) {
        // Validar email único
        if (existeEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado en el sistema, intente con otro.");
        }
        
        // Validar nombre de usuario único
        if (existeNombreUsuario(usuario.getNombre_usuario())) {
            throw new RuntimeException("El nombre de usuario ya existe, por favor elija otro.");
        }
        
        // Validar requisitos mínimos de contraseña
        if (usuario.getContraseña() == null || usuario.getContraseña().length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres.");
        }
        
        // Validar que los vendedores tengan email corporativo
        if (usuario.getTipo_usuario() == TipoUsuario.vendedor) {
            if (!usuario.getEmail().endsWith("@sivil.com")) {
                throw new RuntimeException("Los vendedores deben usar un email corporativo (@sivil.com)");
            }
        }
        
        return usuarioRepository.save(usuario);
    }
}