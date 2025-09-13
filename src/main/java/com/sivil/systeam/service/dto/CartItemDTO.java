package com.sivil.systeam.service.dto;

import java.math.BigDecimal;

public record CartItemDTO(
        Integer libroId,
        String titulo,
        BigDecimal precioUnitario,
        Integer cantidad
) {}
