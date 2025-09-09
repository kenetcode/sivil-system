package com.sivil.systeam.service;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.TipoUsuario;
import com.sivil.systeam.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
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
        
        // Validar formato de nombre de usuario
        String nombreUsuario = usuario.getNombre_usuario();
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new RuntimeException("El nombre de usuario es obligatorio.");
        }
        nombreUsuario = nombreUsuario.trim();
        
        if (nombreUsuario.length() < 3) {
            throw new RuntimeException("El nombre de usuario debe tener al menos 3 caracteres.");
        }
        if (nombreUsuario.length() > 20) {
            throw new RuntimeException("El nombre de usuario no puede tener más de 20 caracteres.");
        }
        if (!nombreUsuario.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            throw new RuntimeException("El nombre de usuario debe empezar con una letra y solo puede contener letras, números y guiones bajos.");
        }
        
        // Validar nombre de usuario único
        if (existeNombreUsuario(nombreUsuario)) {
            throw new RuntimeException("El nombre de usuario ya existe, por favor elija otro.");
        }
        
        // Validar requisitos mínimos de contraseña
        if (usuario.getContraseña() == null || usuario.getContraseña().length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres.");
        }
        
        // Validar que contenga al menos una letra y un número
        if (!usuario.getContraseña().matches(".*[a-zA-Z].*") || !usuario.getContraseña().matches(".*\\d.*")) {
            throw new RuntimeException("La contraseña debe contener al menos una letra y un número.");
        }
        
        // Validar que los vendedores y administradores tengan email corporativo
        if (usuario.getTipo_usuario() == TipoUsuario.vendedor || usuario.getTipo_usuario() == TipoUsuario.admin) {
            if (!usuario.getEmail().endsWith("@sivil.com")) {
                String tipoUsuario = usuario.getTipo_usuario() == TipoUsuario.admin ? "administradores" : "vendedores";
                throw new RuntimeException("Los " + tipoUsuario + " deben usar un email corporativo (@sivil.com)");
            }
        }
        
        return usuarioRepository.save(usuario);
    }
    
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }
}