package com.sivil.systeam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PagesController {

    @GetMapping("/carrito")
    public String carrito() {
        return "carrito";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }

    @GetMapping("/compras")
    public String compras() {
        return "compras";
    }
}
