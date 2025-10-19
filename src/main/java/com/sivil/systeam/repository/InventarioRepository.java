package com.sivil.systeam.repository;

import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.enums.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Libro, Integer> {

    // ============================================================
    // VALIDACIONES / BÚSQUEDAS DIRECTAS (campos con underscore)
    // ============================================================

    // ¿Existe un libro con este código? (para alta y validaciones generales)
    @Query("SELECT (COUNT(l) > 0) FROM Libro l WHERE l.codigo_libro = :codigo")
    boolean existsByCodigo_libro(@Param("codigo") String codigo_libro);

    // ¿Existe otro libro (distinto ID) con el mismo código? (para edición HU014)
    @Query("SELECT (COUNT(l) > 0) FROM Libro l WHERE l.codigo_libro = :codigo AND l.id_libro <> :id")
    boolean existsByCodigo_libroAndId_libroNot(@Param("codigo") String codigo_libro,
                                               @Param("id") Integer id_libro);

    // Buscar por código + estado
    @Query("SELECT l FROM Libro l WHERE l.codigo_libro = :codigo AND l.estado = :estado")
    Libro findByCodigo_libroAndEstado(@Param("codigo") String codigo_libro,
                                      @Param("estado") Estado estado);

    // ============================================================
    // LISTADOS ORDENADOS (fecha_creacion con underscore)
    // ============================================================

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado ORDER BY l.fecha_creacion DESC")
    List<Libro> findByEstadoOrderByFecha_creacionDesc(@Param("estado") Estado estado);

    // ============================================================
    // BÚSQUEDAS DE TEXTO (métodos derivados sin underscore)
    // ============================================================

    List<Libro> findByTituloContainingIgnoreCaseAndEstado(String titulo, Estado estado);

    List<Libro> findByAutorContainingIgnoreCaseAndEstado(String autor, Estado estado);

    List<Libro> findByCategoriaAndEstado(String categoria, Estado estado);

    List<Libro> findByEditorialContainingIgnoreCaseAndEstado(String editorial, Estado estado);

    // ============================================================
    // STOCK (cantidad_stock con underscore)
    // ============================================================

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock > :cantidad")
    List<Libro> findByEstadoAndCantidad_stockGreaterThan(@Param("estado") Estado estado,
                                                         @Param("cantidad") int cantidad);

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock < :cantidad")
    List<Libro> findByEstadoAndCantidad_stockLessThan(@Param("estado") Estado estado,
                                                      @Param("cantidad") int cantidad);

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock = :cantidad")
    List<Libro> findByEstadoAndCantidad_stock(@Param("estado") Estado estado,
                                              @Param("cantidad") int cantidad);

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado ORDER BY l.cantidad_stock ASC")
    List<Libro> findByEstadoOrderByCantidad_stockAsc(@Param("estado") Estado estado);

    // ============================================================
    // ESTADÍSTICAS
    // ============================================================

    long countByEstado(Estado estado);

    @Query("SELECT COUNT(l) FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock < :cantidad")
    long countByEstadoAndCantidad_stockLessThan(@Param("estado") Estado estado,
                                                @Param("cantidad") int cantidad);

    @Query("SELECT COUNT(l) FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock = :cantidad")
    long countByEstadoAndCantidad_stock(@Param("estado") Estado estado,
                                        @Param("cantidad") int cantidad);

    @Query("SELECT SUM(l.cantidad_stock) FROM Libro l WHERE l.estado = :estado")
    Long sumCantidadStockByEstado(@Param("estado") Estado estado);

    @Query("SELECT SUM(l.precio * l.cantidad_stock) FROM Libro l WHERE l.estado = :estado")
    BigDecimal sumValorInventarioByEstado(@Param("estado") Estado estado);

    // ============================================================
    // SOPORTE HU014: Validación de stock vs vendidos
    // ============================================================

    // Suma total de unidades vendidas de un libro (ajusta entidad/campo si difiere)
    @Query("SELECT COALESCE(SUM(dv.cantidad), 0) " +
            "FROM DetalleVenta dv " +
            "WHERE dv.libro.id_libro = :id")
    Long sumCantidadVendidaByLibroId(@Param("id") Integer id_libro);
}
