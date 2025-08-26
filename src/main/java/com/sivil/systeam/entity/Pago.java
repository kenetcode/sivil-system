package com.sivil.systeam.entity;

import com.sivil.systeam.enums.EstadoPago;
import com.sivil.systeam.enums.MetodoPago;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pagos")
@Check(constraints = "((id_compra IS NOT NULL AND id_venta IS NULL) OR " +
        "(id_compra IS NULL AND id_venta IS NOT NULL))")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra",
            foreignKey = @ForeignKey(name = "fk_pagos_compra"))
    private CompraOnline compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta",
            foreignKey = @ForeignKey(name = "fk_pagos_venta"))
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetodoPago metodo_pago;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado_pago = EstadoPago.pendiente;

    @Column(columnDefinition = "TEXT")
    private String datos_tarjeta_encriptados;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_pago;

    @Column(unique = true, length = 100)
    private String referencia_transaccion;

    // Relaciones
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComprobantePago> comprobantes;

    // Constructores
    public Pago() {}

    public Pago(MetodoPago metodo_pago, BigDecimal monto) {
        this.metodo_pago = metodo_pago;
        this.monto = monto;
    }

    // Getters y Setters
    public Integer getId_pago() { return id_pago; }
    public void setId_pago(Integer id_pago) { this.id_pago = id_pago; }

    public CompraOnline getCompra() { return compra; }
    public void setCompra(CompraOnline compra) { this.compra = compra; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public MetodoPago getMetodo_pago() { return metodo_pago; }
    public void setMetodo_pago(MetodoPago metodo_pago) { this.metodo_pago = metodo_pago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public EstadoPago getEstado_pago() { return estado_pago; }
    public void setEstado_pago(EstadoPago estado_pago) { this.estado_pago = estado_pago; }

    public String getDatos_tarjeta_encriptados() { return datos_tarjeta_encriptados; }
    public void setDatos_tarjeta_encriptados(String datos_tarjeta_encriptados) { this.datos_tarjeta_encriptados = datos_tarjeta_encriptados; }

    public LocalDateTime getFecha_pago() { return fecha_pago; }
    public void setFecha_pago(LocalDateTime fecha_pago) { this.fecha_pago = fecha_pago; }

    public String getReferencia_transaccion() { return referencia_transaccion; }
    public void setReferencia_transaccion(String referencia_transaccion) { this.referencia_transaccion = referencia_transaccion; }

    public List<ComprobantePago> getComprobantes() { return comprobantes; }
    public void setComprobantes(List<ComprobantePago> comprobantes) { this.comprobantes = comprobantes; }
}