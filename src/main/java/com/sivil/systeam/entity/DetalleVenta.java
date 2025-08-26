package com.sivil.systeam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_venta")
    private Integer idDetalleVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_venta_venta"))
    @NotNull
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_venta_libro"))
    @NotNull
    private Libro libro;

    @Column(name = "cantidad", nullable = false)
    @Min(value = 1, message = "La cantidad debe ser mayor que 0")
    @NotNull
    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.00")
    @NotNull
    private BigDecimal precioUnitario;

    @Column(name = "subtotal_item", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.00")
    @NotNull
    private BigDecimal subtotalItem;

    // Constructores
    public DetalleVenta() {}

    public DetalleVenta(Venta venta, Libro libro, Integer cantidad,
                        BigDecimal precioUnitario, BigDecimal subtotalItem) {
        this.venta = venta;
        this.libro = libro;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotalItem = subtotalItem;
    }

    // Getters y Setters
    public Integer getIdDetalleVenta() { return idDetalleVenta; }
    public void setIdDetalleVenta(Integer idDetalleVenta) { this.idDetalleVenta = idDetalleVenta; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotalItem() { return subtotalItem; }
    public void setSubtotalItem(BigDecimal subtotalItem) { this.subtotalItem = subtotalItem; }
}