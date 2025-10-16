package com.sivil.systeam.service;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar usuario por su nombre de usuario (campo nombre_usuario en la BD)
        Usuario usuario = usuarioRepository.findByNombreUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Asignar rol basado en el tipo_usuario del registro (admin, vendedor, comprador)
        // ⚠️ Asegúrate de que getTipo_usuario() devuelva un Enum o String como 'admin', 'vendedor', etc.
        String rol = "ROLE_" + usuario.getTipo_usuario().toString().toUpperCase();

        // Crear el UserDetails que usará Spring Security para autenticar
        return User.withUsername(usuario.getNombre_usuario())
                .password(usuario.getContraseña())
                .authorities(Collections.singletonList(() -> rol))
                .build();
    }
}
