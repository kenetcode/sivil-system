package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.service.PagoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pago")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/pago-tarjeta")
    public String pagoTarjeta() {
        return "pago/pago-tarjeta"; // tu vista HTML
    }

    @GetMapping("/tarjeta")
    public String mostrarFormulario(Model model) {
        model.addAttribute("pago", new Pago());
        return "pago/pago-tarjeta";
    }

    @PostMapping("/procesar")
    public String procesarPago(@ModelAttribute Pago pago, Model model) {
        try {
            Pago pagoProcesado = pagoService.procesarPago(pago);

            model.addAttribute("mensaje", "✅ Pago procesado correctamente");
            model.addAttribute("pago", pagoProcesado);

            return "pago/pago-confirmacion"; // vista de confirmación
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensaje", "❌ Error: " + e.getMessage());
            return "pago/pago-tarjeta"; // vuelve al formulario
        }
    }
}