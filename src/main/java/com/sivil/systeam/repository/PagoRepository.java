package com.sivil.systeam.repository;

import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.entity.CompraOnline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("SELECT p FROM Pago p WHERE p.compra = :compra")
    List<Pago> findByCompra(@Param("compra") CompraOnline compra);
}