package com.sivil.systeam.service;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.enums.Estado;
import com.sivil.systeam.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    // ============================================================
    // MÉTODOS QUE USA TU LibroController
    // ============================================================

    @Transactional(readOnly = true)
    public List<Libro> obtenerTodosLosLibros() {
        return libroRepository.findByEstadoOrderByFecha_creacionDesc(Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosActivos() {
        return libroRepository.findByEstadoOrderByFecha_creacionDesc(Estado.activo);
    }

    @Transactional(readOnly = true)
    public Optional<Libro> obtenerLibroPorId(Integer id) {
        return libroRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosPorCategoria(String categoria) {
        return libroRepository.findByCategoriaAndEstado(categoria, Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCaseAndEstado(titulo, Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutorContainingIgnoreCaseAndEstado(autor, Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosConStock() {
        return libroRepository.findByEstadoAndCantidad_stockGreaterThan(Estado.activo, 0);
    }

    // ============================================================
    // HU013: Agregar Libro al Inventario
    // ============================================================

    public Libro guardarLibro(Libro libro) {
        // Siempre se crea activo
        libro.setEstado(Estado.activo);

        // Validar stock inicial (mínimo 1 según HU013)
        if (libro.getCantidad_stock() == null || libro.getCantidad_stock() < 1) {
            throw new IllegalArgumentException("El stock inicial debe ser al menos 1");
        }

        // Validar año de publicación (1900 - año actual)
        Integer anio = libro.getAño_publicacion();
        int anioActual = LocalDateTime.now().getYear();
        if (anio == null || anio < 1900 || anio > anioActual) {
            throw new IllegalArgumentException("El año de publicación debe estar entre 1900 y " + anioActual);
        }

        // Validar precio positivo
        BigDecimal precio = libro.getPrecio();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // Guardar libro en BD
        return libroRepository.save(libro);
    }


    @Transactional(readOnly = true)
    public boolean existePorCodigoLibro(String codigo_libro) {
        return libroRepository.existsByCodigo_libro(codigo_libro);
    }

    @Transactional(readOnly = true)
    public Libro buscarPorCodigoLibro(String codigo_libro) {
        return libroRepository.findByCodigo_libroAndEstado(codigo_libro, Estado.activo);
    }

    // ============================================================
    // STOCK Y ESTADÍSTICAS
    // ============================================================

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosStockBajo() {
        return libroRepository.findByEstadoAndCantidad_stockLessThan(Estado.activo, 5);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasInventario() {
        Map<String, Object> stats = new HashMap<>();

        long totalLibros = libroRepository.countByEstado(Estado.activo);
        stats.put("totalLibros", totalLibros);

        Long totalStock = libroRepository.sumCantidadStockByEstado(Estado.activo);
        stats.put("totalStock", totalStock != null ? totalStock : 0L);

        long librosStockBajo = libroRepository.countByEstadoAndCantidad_stockLessThan(Estado.activo, 5);
        stats.put("librosStockBajo", librosStockBajo);

        long librosSinStock = libroRepository.countByEstadoAndCantidad_stock(Estado.activo, 0);
        stats.put("librosSinStock", librosSinStock);

        BigDecimal valorInventario = libroRepository.sumValorInventarioByEstado(Estado.activo);
        stats.put("valorTotalInventario", valorInventario != null ? valorInventario : BigDecimal.ZERO);

        return stats;
    }

    // ============================================================
    // OPERACIONES DE STOCK
    // ============================================================

    public Libro actualizarStock(String codigo_libro, int nuevaCantidad) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        if (nuevaCantidad < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa");
        libro.setCantidad_stock(nuevaCantidad);
        return libroRepository.save(libro);
    }

    public Libro desactivarLibro(String codigo_libro) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        libro.setEstado(Estado.inactivo);
        return libroRepository.save(libro);
    }

    @Transactional(readOnly = true)
    public boolean validarDisponibilidadStock(String codigo_libro, int cantidadSolicitada) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        return libro != null && libro.getCantidad_stock() >= cantidadSolicitada;
    }

    public void reducirStock(String codigo_libro, int cantidadVendida) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        if (libro.getCantidad_stock() < cantidadVendida) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + libro.getCantidad_stock());
        }
        libro.setCantidad_stock(libro.getCantidad_stock() - cantidadVendida);
        libroRepository.save(libro);
    }

    public void incrementarStock(String codigo_libro, int cantidadAgregar) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        libro.setCantidad_stock(libro.getCantidad_stock() + cantidadAgregar);
        libroRepository.save(libro);
    }
}
