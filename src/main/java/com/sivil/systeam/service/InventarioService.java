package com.sivil.systeam.service;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.enums.Estado;
import com.sivil.systeam.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de Inventario
 * - HU013: Agregar libro
 * - HU014: Actualizar información de libro
 * - Operaciones de stock y estadísticas
 */
@Service
@Transactional
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    // ============================================================
    // MÉTODOS QUE USA TU LibroController / InventarioController
    // ============================================================

    @Transactional(readOnly = true)
    public List<Libro> obtenerTodosLosLibros() {
        return inventarioRepository.findByEstadoOrderByFecha_creacionDesc(Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosActivos() {
        return inventarioRepository.findByEstadoOrderByFecha_creacionDesc(Estado.activo);
    }

    @Transactional(readOnly = true)
    public Optional<Libro> obtenerLibroPorId(Integer id) {
        return inventarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosPorCategoria(String categoria) {
        return inventarioRepository.findByCategoriaAndEstado(categoria, Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> buscarPorTitulo(String titulo) {
        return inventarioRepository.findByTituloContainingIgnoreCaseAndEstado(titulo, Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> buscarPorAutor(String autor) {
        return inventarioRepository.findByAutorContainingIgnoreCaseAndEstado(autor, Estado.activo);
    }

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosConStock() {
        return inventarioRepository.findByEstadoAndCantidad_stockGreaterThan(Estado.activo, 0);
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
        return inventarioRepository.save(libro);
    }

    @Transactional(readOnly = true)
    public boolean existePorCodigoLibro(String codigo_libro) {
        return inventarioRepository.existsByCodigo_libro(codigo_libro);
    }

    @Transactional(readOnly = true)
    public Libro buscarPorCodigoLibro(String codigo_libro) {
        return inventarioRepository.findByCodigo_libroAndEstado(codigo_libro, Estado.activo);
    }

    // ============================================================
    // HU014: Actualizar Información de Libro
    // ============================================================

    @Transactional(readOnly = true)
    public boolean existeOtroConMismoCodigo(Integer idLibro, String codigoNuevo) {
        return inventarioRepository.existsByCodigo_libroAndId_libroNot(codigoNuevo, idLibro);
    }

    /**
     * Actualizar información del libro (HU014)
     * Reglas:
     * - ID debe existir
     * - Código único si cambia (excluyendo este ID)
     * - Año entre 1900 y año actual
     * - Precio > 0
     * - Stock nuevo >= cantidad vendida (si no hay ventas, se toma 0)
     * - No se tocan id ni fecha_creacion (las fechas las manejan triggers)
     * - Se permite cambiar estado (opcional)
     *
     * @param id        ID del libro a actualizar
     * @param cambios   Objeto con nuevos valores (desde el formulario)
     * @param username  Usuario modificador (para logs/notificación)
     * @return Libro actualizado
     */
    public Libro actualizarLibro(Integer id, Libro cambios, String username) {
        Libro actual = inventarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el libro con ID: " + id));

        // Código: obligatorio y único si cambia
        String nuevoCodigo = cambios.getCodigo_libro();
        if (nuevoCodigo == null || nuevoCodigo.isBlank()) {
            throw new IllegalArgumentException("El código del libro es obligatorio");
        }
        if (!nuevoCodigo.equals(actual.getCodigo_libro())) {
            boolean existe = existeOtroConMismoCodigo(id, nuevoCodigo);
            if (existe) {
                throw new IllegalArgumentException("El código '" + nuevoCodigo + "' ya existe en otro libro");
            }
        }

        // Año de publicación
        Integer anio = cambios.getAño_publicacion();
        int anioActual = LocalDateTime.now().getYear();
        if (anio == null || anio < 1900 || anio > anioActual) {
            throw new IllegalArgumentException("El año de publicación debe estar entre 1900 y " + anioActual);
        }

        // Precio
        BigDecimal precio = cambios.getPrecio();
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        // Stock >= vendidos
        Long vendidos;
        try {
            vendidos = inventarioRepository.sumCantidadVendidaByLibroId(id);
        } catch (Exception ignore) {
            vendidos = 0L; // si aún no tienes ventas implementadas, asumimos 0
        }
        Integer stockNuevo = cambios.getCantidad_stock();
        if (stockNuevo == null || stockNuevo < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (vendidos != null && stockNuevo < vendidos.intValue()) {
            throw new IllegalArgumentException("El stock no puede ser menor a la cantidad ya vendida (" + vendidos + ")");
        }

        // Detectar cambios relevantes para notificación
        boolean precioCambio = actual.getPrecio() != null && actual.getPrecio().compareTo(precio) != 0;
        boolean disponibilidadCambio = (actual.getCantidad_stock() == 0 && stockNuevo > 0)
                || (actual.getCantidad_stock() > 0 && stockNuevo == 0);

        // Aplicar cambios permitidos (NO tocar id ni fecha_creacion; triggers manejan auditoría)
        actual.setCodigo_libro(nuevoCodigo);
        actual.setTitulo(cambios.getTitulo());
        actual.setAutor(cambios.getAutor());
        actual.setAño_publicacion(anio);
        actual.setPrecio(precio);
        actual.setCantidad_stock(stockNuevo);
        actual.setCategoria(cambios.getCategoria());
        actual.setEditorial(cambios.getEditorial());
        actual.setDescripcion(cambios.getDescripcion());
        actual.setImagen_url(cambios.getImagen_url());
        actual.setEstado(cambios.getEstado()); // si quieres permitir activar/desactivar en HU014

        Libro guardado = inventarioRepository.save(actual);

        // Notificación simple por consola (puedes integrar email/eventos más adelante)
        if (precioCambio || disponibilidadCambio) {
            System.out.printf("NOTIFICACIÓN: %s actualizó libro %s (ID %d). Cambió precio/disponibilidad.%n",
                    (username != null ? username : "sistema"),
                    guardado.getCodigo_libro(), guardado.getId_libro());
        }

        return guardado;
    }

    // ============================================================
    // STOCK Y ESTADÍSTICAS
    // ============================================================

    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosStockBajo() {
        return inventarioRepository.findByEstadoAndCantidad_stockLessThan(Estado.activo, 5);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasInventario() {
        Map<String, Object> stats = new HashMap<>();

        long totalLibros = inventarioRepository.countByEstado(Estado.activo);
        stats.put("totalLibros", totalLibros);

        Long totalStock = inventarioRepository.sumCantidadStockByEstado(Estado.activo);
        stats.put("totalStock", totalStock != null ? totalStock : 0L);

        long librosStockBajo = inventarioRepository.countByEstadoAndCantidad_stockLessThan(Estado.activo, 5);
        stats.put("librosStockBajo", librosStockBajo);

        long librosSinStock = inventarioRepository.countByEstadoAndCantidad_stock(Estado.activo, 0);
        stats.put("librosSinStock", librosSinStock);

        BigDecimal valorInventario = inventarioRepository.sumValorInventarioByEstado(Estado.activo);
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
        return inventarioRepository.save(libro);
    }

    public Libro desactivarLibro(String codigo_libro) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        libro.setEstado(Estado.inactivo);
        return inventarioRepository.save(libro);
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
        inventarioRepository.save(libro);
    }

    public void incrementarStock(String codigo_libro, int cantidadAgregar) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        libro.setCantidad_stock(libro.getCantidad_stock() + cantidadAgregar);
        inventarioRepository.save(libro);
    }

    // ============================================================
    // ELIMINACIÓN DE LIBROS
    // ============================================================

    public void eliminarLibro(Integer id) {
        Optional<Libro> libroOpt = inventarioRepository.findById(id);
        if (libroOpt.isEmpty()) {
            throw new IllegalArgumentException("Libro no encontrado");
        }

        Libro libro = libroOpt.get();

        // Validar que el libro esté activo
        if (libro.getEstado() != Estado.activo) {
            throw new IllegalArgumentException("No se puede eliminar: el libro no está activo");
        }

        // Validar que el stock sea cero
        if (libro.getCantidad_stock() > 0) {
            throw new IllegalArgumentException("No se puede eliminar: tiene " +
                    libro.getCantidad_stock() + " unidades en stock");
        }

        // Eliminación física del libro de la base de datos
        inventarioRepository.deleteById(id);
    }

    public void eliminarLibroPorCodigo(String codigo_libro) {
        Libro libro = buscarPorCodigoLibro(codigo_libro);
        if (libro == null) {
            throw new IllegalArgumentException("No se encontró el libro con código: " + codigo_libro);
        }
        inventarioRepository.delete(libro);
    }
}
