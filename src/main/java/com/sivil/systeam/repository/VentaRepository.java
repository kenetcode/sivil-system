package com.sivil.systeam.repository;

import com.sivil.systeam.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query("SELECT COUNT(v) > 0 FROM Venta v WHERE v.numero_factura = :numeroFactura")
    boolean existsByNumero_Factura(@Param("numeroFactura") String numeroFactura);
}