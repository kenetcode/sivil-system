package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/libros")
@CrossOrigin(origins = "*") // Permitir CORS para testing
public class LibroController {

    @Autowired
    private LibroService libroService;

    // GET /api/libros - Obtener todos los libros
    @GetMapping
    public ResponseEntity<List<Libro>> obtenerTodosLosLibros() {
        List<Libro> libros = libroService.obtenerTodosLosLibros();
        return ResponseEntity.ok(libros);
    }

    // GET /api/libros/activos - Solo libros activos
    @GetMapping("/activos")
    public ResponseEntity<List<Libro>> obtenerLibrosActivos() {
        List<Libro> libros = libroService.obtenerLibrosActivos();
        return ResponseEntity.ok(libros);
    }

    // GET /api/libros/{id} - Obtener libro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerLibroPorId(@PathVariable("id") Integer id) {
        Optional<Libro> libro = libroService.obtenerLibroPorId(id);

        if (libro.isPresent()) {
            return ResponseEntity.ok(libro.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/libros/categoria/{categoria} - Libros por categoría
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Libro>> obtenerPorCategoria(@PathVariable("categoria") String categoria) {
        List<Libro> libros = libroService.obtenerLibrosPorCategoria(categoria);
        return ResponseEntity.ok(libros);
    }

    // GET /api/libros/buscar/titulo?q=... - Buscar por título
    @GetMapping("/buscar/titulo")
    public ResponseEntity<List<Libro>> buscarPorTitulo(@RequestParam("q") String q) {
        List<Libro> libros = libroService.buscarPorTitulo(q);
        return ResponseEntity.ok(libros);
    }

    // GET /api/libros/buscar/autor?q=... - Buscar por autor
    @GetMapping("/buscar/autor")
    public ResponseEntity<List<Libro>> buscarPorAutor(@RequestParam("q") String q) {
        List<Libro> libros = libroService.buscarPorAutor(q);
        return ResponseEntity.ok(libros);
    }

    // GET /api/libros/disponibles - Solo libros con stock
    @GetMapping("/disponibles")
    public ResponseEntity<List<Libro>> obtenerLibrosDisponibles() {
        List<Libro> libros = libroService.obtenerLibrosConStock();
        return ResponseEntity.ok(libros);
    }
}