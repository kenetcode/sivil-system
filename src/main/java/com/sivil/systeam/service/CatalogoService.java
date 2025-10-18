package com.sivil.systeam.service;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.enums.Estado;
import com.sivil.systeam.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el catálogo de libros disponibles para compra online
 * Implementa búsqueda, filtros, ordenamiento y paginación
 */
@Service
public class CatalogoService {

    @Autowired
    private LibroRepository libroRepository;

    private static final int LIBROS_POR_PAGINA = 12;

    /**
     * Obtiene todos los libros disponibles para el catálogo (activos con stock > 0)
     */
    public List<Libro> obtenerLibrosDisponibles() {
        return libroRepository.findByEstadoAndCantidad_stockGreaterThan(Estado.activo, 0);
    }

    /**
     * Busca y filtra libros con paginación
     * @param busqueda término de búsqueda en título (mínimo 3 caracteres)
     * @param autor filtro por autor específico
     * @param ordenamiento tipo de ordenamiento a aplicar
     * @param pagina número de página (0-based)
     * @return página con libros filtrados y ordenados
     */
    public Page<Libro> buscarYFiltrarLibros(String busqueda, String autor, String ordenamiento, int pagina) {
        // 1. Obtener libros base (activos con stock)
        List<Libro> libros = obtenerLibrosDisponibles();

        // 2. Aplicar búsqueda por título (si se proporciona y tiene al menos 3 caracteres)
        if (busqueda != null && !busqueda.trim().isEmpty() && busqueda.trim().length() >= 3) {
            String busquedaLower = busqueda.toLowerCase().trim();
            libros = libros.stream()
                    .filter(libro -> libro.getTitulo().toLowerCase().contains(busquedaLower))
                    .collect(Collectors.toList());
        }

        // 3. Aplicar filtro por autor (si se proporciona)
        if (autor != null && !autor.trim().isEmpty() && !autor.equals("todos")) {
            libros = libros.stream()
                    .filter(libro -> libro.getAutor().equalsIgnoreCase(autor.trim()))
                    .collect(Collectors.toList());
        }

        // 4. Aplicar ordenamiento
        libros = aplicarOrdenamiento(libros, ordenamiento);

        // 5. Aplicar paginación
        return paginarLibros(libros, pagina);
    }

    /**
     * Aplica el ordenamiento especificado a la lista de libros
     */
    private List<Libro> aplicarOrdenamiento(List<Libro> libros, String ordenamiento) {
        if (ordenamiento == null || ordenamiento.isEmpty()) {
            ordenamiento = "titulo-asc"; // Default
        }

        switch (ordenamiento) {
            case "precio-asc":
                return libros.stream()
                        .sorted(Comparator.comparing(Libro::getPrecio))
                        .collect(Collectors.toList());
            
            case "precio-desc":
                return libros.stream()
                        .sorted(Comparator.comparing(Libro::getPrecio).reversed())
                        .collect(Collectors.toList());
            
            case "titulo-asc":
                return libros.stream()
                        .sorted(Comparator.comparing(libro -> libro.getTitulo().toLowerCase()))
                        .collect(Collectors.toList());
            
            case "titulo-desc":
                return libros.stream()
                        .sorted(Comparator.comparing((Libro libro) -> libro.getTitulo().toLowerCase()).reversed())
                        .collect(Collectors.toList());
            
            case "stock-asc":
                return libros.stream()
                        .sorted(Comparator.comparing(Libro::getCantidad_stock))
                        .collect(Collectors.toList());
            
            case "stock-desc":
                return libros.stream()
                        .sorted(Comparator.comparing(Libro::getCantidad_stock).reversed())
                        .collect(Collectors.toList());
            
            default:
                return libros;
        }
    }

    /**
     * Convierte una lista de libros en una página
     */
    private Page<Libro> paginarLibros(List<Libro> libros, int numeroPagina) {
        Pageable pageable = PageRequest.of(numeroPagina, LIBROS_POR_PAGINA);
        
        int inicio = (int) pageable.getOffset();
        int fin = Math.min((inicio + pageable.getPageSize()), libros.size());
        
        List<Libro> librosPaginados;
        if (inicio > libros.size()) {
            librosPaginados = List.of();
        } else {
            librosPaginados = libros.subList(inicio, fin);
        }
        
        return new PageImpl<>(librosPaginados, pageable, libros.size());
    }

    /**
     * Obtiene la lista de autores únicos de libros disponibles
     */
    public List<String> obtenerAutoresDisponibles() {
        return obtenerLibrosDisponibles().stream()
                .map(Libro::getAutor)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un libro por su ID si está disponible (activo con stock)
     */
    public Libro obtenerLibroDisponiblePorId(Integer id) {
        return libroRepository.findById(id)
                .filter(libro -> libro.isActivo() && libro.tieneStock())
                .orElse(null);
    }

    /**
     * Cuenta total de libros disponibles en el catálogo
     */
    public long contarLibrosDisponibles() {
        return libroRepository.countByEstado(Estado.activo);
    }
}
