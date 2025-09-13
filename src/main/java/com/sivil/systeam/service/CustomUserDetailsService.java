package com.sivil.systeam.service;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Crear autoridad basada en el tipo de usuario
        String authority = "ROLE_" + usuario.getTipo_usuario().name().toUpperCase();

        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getContraseña()) // Sin encriptar como pediste
            .authorities(Collections.singletonList(new SimpleGrantedAuthority(authority)))
            .build();
    }
}