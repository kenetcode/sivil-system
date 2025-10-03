package com.sivil.systeam.repository;

import com.sivil.systeam.entity.ComprobantePago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {
    //  vendedor observe los comprobantes de sus ventas
    List<ComprobantePago> findByPagoVentaVendedor_Id(Integer Vendedor_Id);
}
