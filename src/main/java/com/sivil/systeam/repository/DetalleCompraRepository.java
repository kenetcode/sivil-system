package com.sivil.systeam.repository;

import com.sivil.systeam.entity.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Integer> {
    
    @Query("SELECT d FROM DetalleCompra d JOIN FETCH d.libro WHERE d.compra.id_compra = :idCompra")
    List<DetalleCompra> findByCompraIdCompra(@Param("idCompra") Integer idCompra);


}
