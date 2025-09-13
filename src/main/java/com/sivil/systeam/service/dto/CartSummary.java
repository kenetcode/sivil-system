package com.sivil.systeam.service.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSummary(
        List<CartItemDTO> items,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal total
) {}
