package com.sivil.systeam.controller;

import com.sivil.systeam.service.CartService;
import com.sivil.systeam.service.dto.CartItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // ADD un libro al carrito
    @PostMapping("/add")
    public void addItem(@RequestBody CartItemDTO item) {
        cartService.addItem(item);
    }

    // SHOW libros en el carrito
    @GetMapping("/items")
    public List<CartItemDTO> getItems() {
        return cartService.getItems();
    }

    // Calcular el total
    @GetMapping("/total")
    public BigDecimal getTotal() {
        return cartService.getTotal().setScale(2, RoundingMode.HALF_UP); //  redondeado
    }

    // Eliminar un libro por ID
    @DeleteMapping("/remove/{libroId}")
    public void removeItem(@PathVariable Integer libroId) {
        cartService.removeItem(libroId);
    }

    // Vaciar carrito
    @DeleteMapping("/clear")
    public void clearCart() {
        cartService.clearCart();
    }

    // Resumen del carrito
    @GetMapping("/summary")
    public com.sivil.systeam.service.dto.CartSummary getSummary() {
        var items = cartService.getItems();

        var subtotal = items.stream()
                .map(i -> i.precioUnitario().multiply(new BigDecimal(i.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP); //


        var impuestos = subtotal.multiply(new BigDecimal("0.13"))
                .setScale(2, RoundingMode.HALF_UP); //

        var total = subtotal.add(impuestos)
                .setScale(2, RoundingMode.HALF_UP); //

        return new com.sivil.systeam.service.dto.CartSummary(items, subtotal, impuestos, total);
    }
}

