package com.sivil.systeam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_compra")
    private Integer idDetalleCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_compra_compra"))
    @NotNull
    private CompraOnline compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro", nullable = false,
            foreignKey = @ForeignKey(name = "fk_detalle_compra_libro"))
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
    public DetalleCompra() {}

    public DetalleCompra(CompraOnline compra, Libro libro, Integer cantidad,
                         BigDecimal precioUnitario, BigDecimal subtotalItem) {
        this.compra = compra;
        this.libro = libro;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotalItem = subtotalItem;
    }

    // Getters y Setters
    public Integer getIdDetalleCompra() { return idDetalleCompra; }
    public void setIdDetalleCompra(Integer idDetalleCompra) { this.idDetalleCompra = idDetalleCompra; }

    public CompraOnline getCompra() { return compra; }
    public void setCompra(CompraOnline compra) { this.compra = compra; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getSubtotalItem() { return subtotalItem; }
    public void setSubtotalItem(BigDecimal subtotalItem) { this.subtotalItem = subtotalItem; }
}