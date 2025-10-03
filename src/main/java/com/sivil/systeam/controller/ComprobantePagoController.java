package com.sivil.systeam.controller;

import com.sivil.systeam.entity.ComprobantePago;
import com.sivil.systeam.service.ComprobantePagoService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/comprobantes")
public class ComprobantePagoController {

    private final ComprobantePagoService comprobanteService;

    public ComprobantePagoController(ComprobantePagoService comprobanteService) {
        this.comprobanteService = comprobanteService;
    }

    // Subir comprobante PDF
    @PostMapping("/upload/{pagoId}")
    public ResponseEntity<?> subirComprobante(
            @PathVariable Integer pagoId,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        try {
            ComprobantePago comprobante = comprobanteService.guardarComprobante(pagoId, archivo);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Listar comprobantes por vendedor
    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<List<ComprobantePago>> listarComprobantes(@PathVariable Integer vendedorId) {
        return ResponseEntity.ok(comprobanteService.listarPorVendedor(vendedorId));
    }

    // Ver comprobante en navegador
    @GetMapping("/ver/{idComprobante}")
    public ResponseEntity<Resource> verComprobante(@PathVariable Integer idComprobante) {
        return comprobanteService.obtenerArchivoComprobante(idComprobante, false);
    }

    // Descargar comprobante
    @GetMapping("/descargar/{idComprobante}")
    public ResponseEntity<Resource> descargarComprobante(@PathVariable Integer idComprobante) {
        return comprobanteService.obtenerArchivoComprobante(idComprobante, true);
    }
}



