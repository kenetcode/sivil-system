package com.sivil.systeam.service;

import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.repository.PagoRepository;
import org.springframework.stereotype.Service;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    public PagoServiceImpl(PagoRepository pagoRepository) {
        this.pagoRepository = pagoRepository;
    }

    @Override
    public Pago procesarPago(Pago pago) {
        // Aquí encriptamos los datos de la tarjeta
        if (pago.getDatos_tarjeta_encriptados() == null || pago.getDatos_tarjeta_encriptados().isEmpty()) {
            throw new IllegalArgumentException("Datos de tarjeta inválidos");
        }

        // Podrías aplicar más validaciones o encriptación real
        String datosEncriptados = "ENCRYPTED(" + pago.getDatos_tarjeta_encriptados() + ")";
        pago.setDatos_tarjeta_encriptados(datosEncriptados);

        // Guardar en la base de datos
        return pagoRepository.save(pago);
    }
}
