package com.sivil.systeam.repository;

import com.sivil.systeam.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
    Optional<Usuario> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.nombre_usuario = :nombre_usuario")
    boolean existsByNombreUsuario(@Param("nombre_usuario") String nombre_usuario);
    
    Optional<Usuario> findByEmailAndContraseña(String email, String contraseña);
}