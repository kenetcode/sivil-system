package com.sivil.systeam.repository;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.enums.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Integer> {

    // Métodos básicos que se heredan automáticamente:
    // - findAll() - todos los libros
    // - findById(Integer id) - libro por ID
    // - save(Libro libro) - guardar libro
    // - deleteById(Integer id) - eliminar por ID

    // Métodos personalizados
    List<Libro> findByEstado(Estado estado);
    List<Libro> findByCategoria(String categoria);
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
    List<Libro> findByAutorContainingIgnoreCase(String autor);
    List<Libro> findByCantidadStockGreaterThan(Integer cantidad);
}