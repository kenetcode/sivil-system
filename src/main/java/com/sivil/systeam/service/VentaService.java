package com.sivil.systeam.service;

import com.sivil.systeam.entity.DetalleVenta;
import com.sivil.systeam.entity.Libro;
import com.sivil.systeam.entity.Venta;
import com.sivil.systeam.enums.EstadoVenta;
import com.sivil.systeam.repository.DetalleVentaRepository;
import com.sivil.systeam.repository.LibroRepository;
import com.sivil.systeam.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final LibroRepository libroRepository;

    @Autowired
    public VentaService(VentaRepository ventaRepository,
                        DetalleVentaRepository detalleVentaRepository,
                        LibroRepository libroRepository) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.libroRepository = libroRepository;
    }

    /** Listar ventas FINALIZADAS (arreglo del bug) */
    public List<Venta> listarVentasFinalizadas() {
        return ventaRepository.findByEstadoOrderByFechaVentaDesc(EstadoVenta.finalizada);
    }

    /** Listar ventas VISIBLES (excluye INACTIVAS) para búsquedas/listados generales */
    public List<Venta> listarVentasVisibles() {
        return ventaRepository.findAllVisiblesOrderByFechaVentaDesc();
    }

    /** Obtener venta por ID */
    public Optional<Venta> obtenerVentaPorId(Integer id) {
        return ventaRepository.findById(id);
    }

    /** Guardar/actualizar */
    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    /** HU004: Inactivar por número de factura + restaurar inventario */
    @Transactional
    public void inactivarPorNumeroFactura(String numeroFactura, String motivo) {
        Venta venta = ventaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new IllegalArgumentException("No existe la venta con factura " + numeroFactura));

        if (venta.getEstado() == EstadoVenta.inactiva) {
            throw new IllegalStateException("La venta ya está inactiva.");
        }

        // Restaurar stock por cada detalle
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdWithLibro(venta.getId_venta());
        for (DetalleVenta d : detalles) {
            Libro libro = d.getLibro();
            int stockActual = (libro.getCantidad_stock() == null) ? 0 : libro.getCantidad_stock();
            libro.setCantidad_stock(stockActual + d.getCantidad());
            libroRepository.save(libro);
        }

        // Marcar venta y guardar motivo
        venta.setEstado(EstadoVenta.inactiva);
        venta.setMotivo_inactivacion(motivo);
        ventaRepository.save(venta);
    }

    /** Reactivar venta inactiva + descontar inventario nuevamente */
    @Transactional
    public void reactivarPorNumeroFactura(String numeroFactura) {
        Venta venta = ventaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new IllegalArgumentException("No existe la venta con factura " + numeroFactura));

        if (venta.getEstado() != EstadoVenta.inactiva) {
            throw new IllegalStateException("Solo se pueden reactivar ventas inactivas.");
        }

        // Verificar stock disponible antes de reactivar
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaIdWithLibro(venta.getId_venta());
        for (DetalleVenta d : detalles) {
            Libro libro = d.getLibro();
            int stockActual = (libro.getCantidad_stock() == null) ? 0 : libro.getCantidad_stock();
            if (stockActual < d.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para reactivar. Libro: " + libro.getTitulo() + 
                        ". Stock disponible: " + stockActual + ". Cantidad requerida: " + d.getCantidad());
            }
        }

        // Descontar stock por cada detalle
        for (DetalleVenta d : detalles) {
            Libro libro = d.getLibro();
            int stockActual = (libro.getCantidad_stock() == null) ? 0 : libro.getCantidad_stock();
            libro.setCantidad_stock(stockActual - d.getCantidad());
            libroRepository.save(libro);
        }

        // Reactivar venta como finalizada
        venta.setEstado(EstadoVenta.finalizada);
        venta.setMotivo_inactivacion(null); // Limpiar motivo
        ventaRepository.save(venta);
    }

}
