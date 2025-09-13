package com.sivil.systeam.service;

import com.sivil.systeam.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NumeracionFacturaService {

    @Value("${sucursal.numero:01}")
    private String sucursal;

    private Long ultimoCorrelativo = null;

    public synchronized String generarNumeroFactura(VentaRepository ventaRepository) {
        // SIEMPRE obtener el último correlativo de la base de datos
        ultimoCorrelativo = obtenerUltimoCorrelativo(ventaRepository);

        // INCREMENTAR para obtener el siguiente número
        ultimoCorrelativo++;

        return sucursal + "-" + String.format("%010d", ultimoCorrelativo);
    }

    private Long obtenerUltimoCorrelativo(VentaRepository ventaRepository) {
        try {
            String prefijoBusqueda = sucursal + "-";
            String ultimaFactura = ventaRepository.findTopByNumeroFacturaStartingWith(prefijoBusqueda);

            if (ultimaFactura != null) {
                // Extraer el número correlativo (después del guión)
                String correlativoStr = ultimaFactura.substring(prefijoBusqueda.length());
                return Long.parseLong(correlativoStr);
            }
        } catch (Exception e) {
            // Si hay error, empezar desde 0
            return 0L;
        }

        // Si no hay facturas previas, empezar desde 0
        return 0L;
    }
}