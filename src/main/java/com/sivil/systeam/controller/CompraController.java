// ARCHIVO OBSOLETO - NO SE USA MÁS - Reemplazado por nuevo sistema de compras online
package com.sivil.systeam.controller;

import com.sivil.systeam.entity.CompraOnline;
import com.sivil.systeam.repository.CompraOnlineRepository;
import com.sivil.systeam.service.CompraService;
import com.sivil.systeam.service.dto.CheckoutRequest;
import com.sivil.systeam.service.dto.CheckoutResponse;
import com.sivil.systeam.service.dto.CompraResumenDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compra")
public class CompraController {

    private final CompraService compraService;
    private final CompraOnlineRepository compraOnlineRepository;

    public CompraController(CompraService compraService,
                            CompraOnlineRepository compraOnlineRepository) {
        this.compraService = compraService;
        this.compraOnlineRepository = compraOnlineRepository;
    }

    // 1) Finalizar compra (usa el Service con @Transactional)
    @PostMapping("/checkout")
    public CheckoutResponse checkout(@RequestBody CheckoutRequest request) {
        return compraService.checkout(request);
    }

    // 2) Listado general (resumen)
    @GetMapping("/listar")
    public List<CompraResumenDTO> listarCompras() {
        return compraOnlineRepository.findAllWithComprador()
                .stream().map(this::toResumen).toList();
    }

    // 3) Historial del usuario (resumen)
    @GetMapping("/mis-compras/{usuarioId}")
    public List<CompraResumenDTO> misCompras(@PathVariable Integer usuarioId) {
        return compraOnlineRepository.findByCompradorIdOrderByFechaDesc(usuarioId)
                .stream().map(this::toResumen).toList();
    }

    // 4) Buscar por número de orden (opcional)
    @GetMapping("/{numeroOrden}")
    public CompraResumenDTO buscarPorNumero(@PathVariable String numeroOrden) {
        var compra = compraOnlineRepository.findByNumeroOrden(numeroOrden)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada"));
        return toResumen(compra);
    }

    // Mapper a DTO (con null-safety)
    private CompraResumenDTO toResumen(CompraOnline c) {
        return new CompraResumenDTO(
                c.getNumero_orden(),
                c.getComprador() != null ? c.getComprador().getNombre_completo() : null,
                c.getSubtotal(),
                c.getImpuestos(),
                c.getTotal(),
                c.getEstado_compra() != null ? c.getEstado_compra().name() : null,
                c.getFecha_compra()
        );
    }
}
