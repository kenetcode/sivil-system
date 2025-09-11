package com.sivil.systeam.service;

import com.sivil.systeam.entity.CompraOnline;
import com.sivil.systeam.entity.DetalleCompra;
import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.EstadoCompra;
import com.sivil.systeam.repository.CompraOnlineRepository;
import com.sivil.systeam.repository.DetalleCompraRepository;
import com.sivil.systeam.repository.LibroRepository;
import com.sivil.systeam.repository.UsuarioRepository;
import com.sivil.systeam.service.dto.CartItemDTO;
import com.sivil.systeam.service.dto.CheckoutRequest;
import com.sivil.systeam.service.dto.CheckoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CompraService {

    @Autowired
    private CompraOnlineRepository compraOnlineRepository;


    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private DetalleCompraRepository detalleCompraRepository;

    private static final BigDecimal IVA = new BigDecimal("0.13");

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        // 1) Comprador
        Usuario comprador = usuarioRepository.findById(request.compradorId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2) Subtotal (sumatoria de precio * cantidad)
        BigDecimal subtotal = request.items().stream()
                .map(i -> i.precioUnitario().multiply(BigDecimal.valueOf(i.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 3) Impuestos y total
        BigDecimal impuestos = subtotal.multiply(IVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total     = subtotal.add(impuestos).setScale(2, RoundingMode.HALF_UP);

        // 4) Número de orden
        String numeroOrden = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 5) Cabecera (Compra)
        CompraOnline compra = new CompraOnline();
        compra.setNumero_orden(numeroOrden);
        compra.setComprador(comprador);
        compra.setSubtotal(subtotal);
        compra.setImpuestos(impuestos);
        compra.setTotal(total);
        compra.setDireccion_entrega(request.direccionEntrega());
        compra.setEstado_compra(EstadoCompra.pendiente);
        compra.setMetodo_pago(request.metodoPago());

        compra = compraOnlineRepository.save(compra); // persistimos para tener id_compra

        // 6) Detalles
        for (CartItemDTO item : request.items()) {
            Libro libro = libroRepository.findById(item.libroId())
                    .orElseThrow(() -> new RuntimeException("Libro no encontrado: " + item.libroId()));

            BigDecimal precioUnit = item.precioUnitario().setScale(2, RoundingMode.HALF_UP);
            BigDecimal subLinea   = precioUnit.multiply(BigDecimal.valueOf(item.cantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            DetalleCompra det = new DetalleCompra();
            det.setCompra(compra);
            det.setLibro(libro);
            det.setCantidad(item.cantidad());
            det.setPrecio_unitario(precioUnit);
            det.setSubtotal_item(subLinea);

            detalleCompraRepository.save(det);
        }

        // 7) Respuesta
        return new CheckoutResponse(
                numeroOrden,
                subtotal,
                impuestos,
                total,
                LocalDateTime.now()
        );
    }
}
