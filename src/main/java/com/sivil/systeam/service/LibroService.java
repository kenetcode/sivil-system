package com.sivil.systeam.service;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.repository.LibroRepository;
import com.sivil.systeam.enums.Estado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    // Obtener todos los libros
    public List<Libro> obtenerTodosLosLibros() {
        return libroRepository.findAll();
    }

    // Obtener solo libros activos
    public List<Libro> obtenerLibrosActivos() {
        return libroRepository.findByEstado(Estado.ACTIVO);
    }

    // Obtener libro por ID
    public Optional<Libro> obtenerLibroPorId(Integer id) {
        return libroRepository.findById(id);
    }

    // Obtener libros por categoría
    public List<Libro> obtenerLibrosPorCategoria(String categoria) {
        return libroRepository.findByCategoria(categoria);
    }

    // Buscar libros por título
    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCase(titulo);
    }

    // Buscar libros por autor
    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutorContainingIgnoreCase(autor);
    }

    // Obtener libros con stock disponible
    public List<Libro> obtenerLibrosConStock() {
        return libroRepository.findByCantidadStockGreaterThan(0);
    }
}