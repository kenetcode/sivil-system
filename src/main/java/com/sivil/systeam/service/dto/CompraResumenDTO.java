package com.sivil.systeam.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompraResumenDTO(
        String numeroOrden,
        String compradorNombre,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal total,
        String estado,
        LocalDateTime fecha
) {}
