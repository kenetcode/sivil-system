package com.sivil.systeam.repository;

import com.sivil.systeam.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    @Query("""
           SELECT d FROM DetalleVenta d
           JOIN FETCH d.libro
           WHERE d.venta.id_venta = :idVenta
           """)

    List<DetalleVenta> findByVentaIdWithLibro(@Param("idVenta") Integer idVenta);
}
