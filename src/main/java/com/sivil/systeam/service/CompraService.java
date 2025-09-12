package com.sivil.systeam.service;

import com.sivil.systeam.entity.CompraOnline;
import com.sivil.systeam.entity.DetalleCompra;
import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.EstadoCompra;
import com.sivil.systeam.enums.MetodoPago;
import com.sivil.systeam.repository.CompraOnlineRepository;
import com.sivil.systeam.repository.DetalleCompraRepository;
import com.sivil.systeam.repository.LibroRepository;
import com.sivil.systeam.repository.UsuarioRepository;
import com.sivil.systeam.service.dto.CartItemDTO;
import com.sivil.systeam.service.dto.CheckoutRequest;
import com.sivil.systeam.service.dto.CheckoutResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CompraService {

    @Autowired private CompraOnlineRepository compraOnlineRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LibroRepository libroRepository;
    @Autowired private DetalleCompraRepository detalleCompraRepository;

    private static final BigDecimal IVA = new BigDecimal("0.13");

    /**
     * Checkout de compra online.
     * - Valida carrito y método de pago
     * - Bloquea y descuenta stock
     * - Calcula totales
     * - Define estado_compra según método de pago
     * - Guarda compra y detalles
     */
    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        // ===== 1) Validaciones base =====
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El carrito está vacío.");
        }
        if (request.metodoPago() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Método de pago requerido.");
        }

        // ===== 2) Comprador =====
        Usuario comprador = usuarioRepository.findById(request.compradorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        // ===== 3) Validar/Descontar stock y calcular subtotal =====
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemDTO item : request.items()) {
            Integer cantidad = item.cantidad();
            if (cantidad == null || cantidad <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad inválida.");
            }

            // Bloqueo pesimista para evitar carreras de stock
            Libro libro = libroRepository.lockByIdForUpdate(item.libroId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Libro no existe: " + item.libroId()
                    ));

            int disponible = (libro.getCantidad_stock() == null) ? 0 : libro.getCantidad_stock();
            if (disponible < cantidad) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para '" + libro.getTitulo() + "'. Máximo disponible: " + disponible
                );
            }

            // Descontar stock bajo bloqueo
            libro.setCantidad_stock(disponible - cantidad);

            // Precio desde el libro (si tu DTO trae precio y prefieres usarlo, reemplaza por item.precioUnitario())
            BigDecimal precio = (libro.getPrecio() == null) ? BigDecimal.ZERO : libro.getPrecio();
            subtotal = subtotal.add(precio.multiply(BigDecimal.valueOf(cantidad)));
        }

        subtotal  = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuestos = subtotal.multiply(IVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total     = subtotal.add(impuestos).setScale(2, RoundingMode.HALF_UP);

        // ===== 4) Número de orden =====
        String numeroOrden = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // ===== 5) Cabecera (Compra) =====
        CompraOnline compra = new CompraOnline();
        compra.setNumero_orden(numeroOrden);
        compra.setComprador(comprador);
        compra.setSubtotal(subtotal);
        compra.setImpuestos(impuestos);
        compra.setTotal(total);
        compra.setDireccion_entrega(request.direccionEntrega());
        compra.setMetodo_pago(request.metodoPago());

        // Estado de compra según método (sin tocar la BD)
        if (compra.getMetodo_pago() == MetodoPago.tarjeta) {
            compra.setEstado_compra(EstadoCompra.procesada);
        } else {
            compra.setEstado_compra(EstadoCompra.pendiente);
        }

        compraOnlineRepository.save(compra); // persistimos para tener id_compra

        // ===== 6) Detalles =====
        for (CartItemDTO item : request.items()) {
            Libro libro = libroRepository.findById(item.libroId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Libro no existe: " + item.libroId()
                    ));

            // Si quieres que el detalle refleje el precio del momento:
            BigDecimal precioUnit = (libro.getPrecio() == null)
                    ? BigDecimal.ZERO
                    : libro.getPrecio().setScale(2, RoundingMode.HALF_UP);

            BigDecimal subLinea = precioUnit.multiply(BigDecimal.valueOf(item.cantidad()))
                    .setScale(2, RoundingMode.HALF_UP);

            DetalleCompra det = new DetalleCompra();
            det.setCompra(compra);
            det.setLibro(libro);
            det.setCantidad(item.cantidad());
            det.setPrecio_unitario(precioUnit);
            det.setSubtotal_item(subLinea);

            detalleCompraRepository.save(det);
        }

        // ===== 7) Respuesta =====
        return new CheckoutResponse(
                numeroOrden,
                subtotal,
                impuestos,
                total,
                LocalDateTime.now()
        );
    }
}
