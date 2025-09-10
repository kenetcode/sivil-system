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
public interface LibroRepository extends JpaRepository<Libro, Integer> {

    // ======== VALIDACIONES / BÚSQUEDAS DIRECTAS (con underscore) ========
    @Query("SELECT (COUNT(l) > 0) FROM Libro l WHERE l.codigo_libro = :codigo")
    boolean existsByCodigo_libro(@Param("codigo") String codigo_libro);

    @Query("SELECT l FROM Libro l WHERE l.codigo_libro = :codigo AND l.estado = :estado")
    Libro findByCodigo_libroAndEstado(@Param("codigo") String codigo_libro, @Param("estado") Estado estado);

    // ======== LISTADOS ORDENADOS (fecha_creacion tiene underscore) ========
    @Query("SELECT l FROM Libro l WHERE l.estado = :estado ORDER BY l.fecha_creacion DESC")
    List<Libro> findByEstadoOrderByFecha_creacionDesc(@Param("estado") Estado estado);

    // ======== BÚSQUEDAS DE TEXTO (sin underscore) ========
    List<Libro> findByTituloContainingIgnoreCaseAndEstado(String titulo, Estado estado);
    List<Libro> findByAutorContainingIgnoreCaseAndEstado(String autor, Estado estado);
    List<Libro> findByCategoriaAndEstado(String categoria, Estado estado);
    List<Libro> findByEditorialContainingIgnoreCaseAndEstado(String editorial, Estado estado);

    // ======== STOCK (cantidad_stock tiene underscore) ========
    @Query("SELECT l FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock > :cantidad")
    List<Libro> findByEstadoAndCantidad_stockGreaterThan(@Param("estado") Estado estado, @Param("cantidad") int cantidad);

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock < :cantidad")
    List<Libro> findByEstadoAndCantidad_stockLessThan(@Param("estado") Estado estado, @Param("cantidad") int cantidad);

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock = :cantidad")
    List<Libro> findByEstadoAndCantidad_stock(@Param("estado") Estado estado, @Param("cantidad") int cantidad);

    @Query("SELECT l FROM Libro l WHERE l.estado = :estado ORDER BY l.cantidad_stock ASC")
    List<Libro> findByEstadoOrderByCantidad_stockAsc(@Param("estado") Estado estado);

    // ======== ESTADÍSTICAS (tipos correctos) ========
    long countByEstado(Estado estado);

    @Query("SELECT COUNT(l) FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock < :cantidad")
    long countByEstadoAndCantidad_stockLessThan(@Param("estado") Estado estado, @Param("cantidad") int cantidad);

    @Query("SELECT COUNT(l) FROM Libro l WHERE l.estado = :estado AND l.cantidad_stock = :cantidad")
    long countByEstadoAndCantidad_stock(@Param("estado") Estado estado, @Param("cantidad") int cantidad);

    @Query("SELECT SUM(l.cantidad_stock) FROM Libro l WHERE l.estado = :estado")
    Long sumCantidadStockByEstado(@Param("estado") Estado estado);

    @Query("SELECT SUM(l.precio * l.cantidad_stock) FROM Libro l WHERE l.estado = :estado")
    BigDecimal sumValorInventarioByEstado(@Param("estado") Estado estado);
}
