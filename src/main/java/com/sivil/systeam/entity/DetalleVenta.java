package com.sivil.systeam.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_venta")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_detalle_venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_venta_venta"))
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_venta_libro"))
    private Libro libro;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio_unitario;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal_item;

    // Constructores
    public DetalleVenta() {}

    public DetalleVenta(Venta venta, Libro libro, Integer cantidad,
                        BigDecimal precio_unitario, BigDecimal subtotal_item) {
        this.venta = venta;
        this.libro = libro;
        this.cantidad = cantidad;
        this.precio_unitario = precio_unitario;
        this.subtotal_item = subtotal_item;
    }

    // Getters y Setters
    public Integer getId_detalle_venta() { return id_detalle_venta; }
    public void setId_detalle_venta(Integer id_detalle_venta) { this.id_detalle_venta = id_detalle_venta; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecio_unitario() { return precio_unitario; }
    public void setPrecio_unitario(BigDecimal precio_unitario) { this.precio_unitario = precio_unitario; }

    public BigDecimal getSubtotal_item() { return subtotal_item; }
    public void setSubtotal_item(BigDecimal subtotal_item) { this.subtotal_item = subtotal_item; }
}