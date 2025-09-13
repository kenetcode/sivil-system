package com.sivil.systeam.service;

import com.sivil.systeam.service.dto.CartItemDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    // Lista interna que representa el carrito en memoria
    private final List<CartItemDTO> cartItems = new ArrayList<>();

    // Agregar un libro al carrito (evitar duplicados)
    public void addItem(CartItemDTO newItem) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItemDTO item = cartItems.get(i);
            if (item.libroId().equals(newItem.libroId())) {
                // Si ya existe el libro, sumamos cantidades
                int nuevaCantidad = item.cantidad() + newItem.cantidad();
                cartItems.set(i, new CartItemDTO(
                        item.libroId(),
                        item.titulo(),
                        item.precioUnitario(),
                        nuevaCantidad
                ));
                return; // salir, ya actualizamos
            }
        }
        // Si no existe, lo agregamos al carrito
        cartItems.add(newItem);
    }

    // Quitar un libro por su ID
    public void removeItem(Integer libroId) {
        cartItems.removeIf(it -> it.libroId().equals(libroId));
    }

    // Obtener todos los ítems del carrito
    public List<CartItemDTO> getItems() {
        return new ArrayList<>(cartItems);
    }

    // Calcular el total del carrito
    public BigDecimal getTotal() {
        return cartItems.stream()
                .map(it -> it.precioUnitario().multiply(BigDecimal.valueOf(it.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Vaciar carrito
    public void clearCart() {
        cartItems.clear();
    }
}

