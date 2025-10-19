package com.sivil.systeam.repository;

import com.sivil.systeam.entity.Venta;
import com.sivil.systeam.enums.EstadoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // Listar por estado (método derivado válido)
    List<Venta> findByEstado(EstadoVenta estado);

    // ¿Existe una venta con ese número de factura?
    @Query("SELECT COUNT(v) > 0 FROM Venta v WHERE v.numero_factura = :numeroFactura")
    boolean existsByNumeroFactura(@Param("numeroFactura") String numeroFactura);

    // Últimos números de factura por prefijo (corrige el LIKE con CONCAT)
    @Query("SELECT v.numero_factura FROM Venta v " +
            "WHERE v.numero_factura LIKE CONCAT(:prefijo, '%') " +
            "ORDER BY v.numero_factura DESC")
    List<String> findTopByNumeroFacturaStartingWith(@Param("prefijo") String prefijo);

    // Listar por estado ordenado por fecha de venta desc
    @Query("SELECT v FROM Venta v WHERE v.estado = :estado ORDER BY v.fecha_venta DESC")
    List<Venta> findByEstadoOrderByFechaVentaDesc(@Param("estado") EstadoVenta estado);

    // Buscar una venta por su número de factura (sin underscores en el nombre del método)
    @Query("SELECT v FROM Venta v WHERE v.numero_factura = :numeroFactura")
    Optional<Venta> findByNumeroFactura(@Param("numeroFactura") String numeroFactura);

    @Query("SELECT v FROM Venta v WHERE v.estado <> com.sivil.systeam.enums.EstadoVenta.inactiva ORDER BY v.fecha_venta DESC")
    List<Venta> findAllVisiblesOrderByFechaVentaDesc();

    // Búsqueda por número de factura y/o nombre de cliente
    @Query("SELECT v FROM Venta v WHERE " +
            "(:numeroFactura IS NULL OR LOWER(v.numero_factura) LIKE LOWER(CONCAT('%', :numeroFactura, '%'))) AND " +
            "(:nombreCliente IS NULL OR LOWER(v.nombre_cliente) LIKE LOWER(CONCAT('%', :nombreCliente, '%'))) " +
            "ORDER BY v.fecha_venta DESC NULLS LAST")
    List<Venta> buscarPorCriterios(
            @Param("numeroFactura") String numeroFactura,
            @Param("nombreCliente") String nombreCliente
    );
}
