// src/main/java/com/sivil/systeam/service/dto/CheckoutResponse.java
package com.sivil.systeam.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckoutResponse(
        String numeroOrden,
        BigDecimal subtotal,
        BigDecimal impuestos,
        BigDecimal total,
        LocalDateTime fecha
) {}
