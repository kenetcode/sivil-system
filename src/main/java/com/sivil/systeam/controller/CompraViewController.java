// src/main/java/com/sivil/systeam/controller/CompraViewController.java
package com.sivil.systeam.controller;

import com.sivil.systeam.repository.CompraOnlineRepository;
import com.sivil.systeam.service.dto.CompraResumenDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/compras")
public class CompraViewController {

    private final CompraOnlineRepository compraOnlineRepository;

    public CompraViewController(CompraOnlineRepository compraOnlineRepository) {
        this.compraOnlineRepository = compraOnlineRepository;
    }

    // /compras/lista -> templates/compras-lista.html
    @GetMapping("/lista")
    public String listarCompras(Model model) {
        List<CompraResumenDTO> items = compraOnlineRepository.findAll().stream()
                .map(c -> new CompraResumenDTO(
                        c.getNumero_orden(),
                        c.getComprador() != null ? c.getComprador().getNombre_completo() : null,
                        c.getSubtotal(), c.getImpuestos(), c.getTotal(),
                        c.getEstado_compra() != null ? c.getEstado_compra().name() : null,
                        c.getFecha_compra()
                ))
                .toList();

        model.addAttribute("title", "Compras Online");
        model.addAttribute("subtitle", "Listado de compras registradas");
        model.addAttribute("items", items);
        model.addAttribute("columns", List.of(
                "numeroOrden","compradorNombre","subtotal","impuestos","total","estado","fecha"
        ));
        model.addAttribute("showActions", false);
        model.addAttribute("showAddButton", false);
        model.addAttribute("showViewButton", false);
        model.addAttribute("showEditButton", false);
        model.addAttribute("showDeleteButton", false);

        return "Compra/compras";
    }

    // /compras/carrito -> templates/Compra/carrito.html
    @GetMapping("/carrito")
    public String carrito() {
        return "Compra/carrito";
    }

    // /compras/checkout -> templates/Compra/checkout.html
    @GetMapping("/checkout")
    public String checkout() {
        return "Compra/checkout";
    }
}
