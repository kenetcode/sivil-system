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


    @Query("SELECT u FROM Usuario u WHERE u.nombre_usuario = :username")
    Optional<Usuario> findByNombreUsuario(@Param("username") String username);

    // Métodos de búsqueda por diferentes criterios
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.nombre_completo) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
           "LOWER(u.nombre_usuario) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :criterio, '%'))")
    java.util.List<Usuario> buscarPorCriterioGeneral(@Param("criterio") String criterio);
    
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.nombre_completo) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    java.util.List<Usuario> buscarPorNombre(@Param("nombre") String nombre);
    
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    java.util.List<Usuario> buscarPorEmail(@Param("email") String email);
    
    @Query("SELECT u FROM Usuario u WHERE u.tipo_usuario = :tipoUsuario")
    java.util.List<Usuario> buscarPorTipoUsuario(@Param("tipoUsuario") com.sivil.systeam.enums.TipoUsuario tipoUsuario);

}