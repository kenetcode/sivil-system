package com.sivil.systeam.service;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.Estado;
import com.sivil.systeam.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar usuario por su email (coincide con el input 'username' del formulario)
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Verificar si el usuario está inactivo
        if (usuario.getEstado() == Estado.inactivo) {
            throw new DisabledException("Su cuenta ha sido inactivada. Por favor, contacte con soporte para verificar su problema.");
        }

        // Asignar rol basado en el tipo_usuario (por ejemplo: ADMIN, VENDEDOR, COMPRADOR)
        String authority = "ROLE_" + usuario.getTipo_usuario().name().toUpperCase();

        // Construir objeto UserDetails para Spring Security
        return User.builder()
                .username(usuario.getEmail())              // Email como identificador principal
                .password(usuario.getContraseña())         // Contraseña sin encriptar (NoOpPasswordEncoder)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
