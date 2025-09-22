package com.sivil.systeam.service;

import com.sivil.systeam.entity.Venta;
import com.sivil.systeam.enums.EstadoVenta;
import com.sivil.systeam.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    // Listar todas las ventas finalizadas en orden descendente por fecha
    public List<Venta> listarVentasFinalizadas() {
        return  ventaRepository.findByEstadoOrderByFechaVentaDesc(EstadoVenta.activa);

    }
}
