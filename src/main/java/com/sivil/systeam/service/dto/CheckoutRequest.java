// src/main/java/com/sivil/systeam/service/dto/CheckoutRequest.java
package com.sivil.systeam.service.dto;

import com.sivil.systeam.enums.MetodoPago;
import java.util.List;

public record CheckoutRequest(
        Integer compradorId,
        String direccionEntrega,
        MetodoPago metodoPago,
        List<CartItemDTO> items
) {}
