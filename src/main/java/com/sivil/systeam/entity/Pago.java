package com.sivil.systeam.entity;

import com.sivil.systeam.enums.EstadoPago;
import com.sivil.systeam.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

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
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compra",
            foreignKey = @ForeignKey(name = "fk_pagos_compra"))
    private CompraOnline compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta",
            foreignKey = @ForeignKey(name = "fk_pagos_venta"))
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    @NotNull
    private MetodoPago metodoPago;

    @Column(name = "monto", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.00")
    @NotNull
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "datos_tarjeta_encriptados", columnDefinition = "TEXT")
    private String datosTarjetaEncriptados;

    @CreationTimestamp
    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "referencia_transaccion", unique = true, length = 100)
    @Size(max = 100)
    private String referenciaTransaccion;

    // Relaciones
    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComprobantePago> comprobantes;

    // Constructores
    public Pago() {}

    public Pago(MetodoPago metodoPago, BigDecimal monto) {
        this.metodoPago = metodoPago;
        this.monto = monto;
    }

    // Getters y Setters
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }

    public CompraOnline getCompra() { return compra; }
    public void setCompra(CompraOnline compra) { this.compra = compra; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }

    public String getDatosTarjetaEncriptados() { return datosTarjetaEncriptados; }
    public void setDatosTarjetaEncriptados(String datosTarjetaEncriptados) { this.datosTarjetaEncriptados = datosTarjetaEncriptados; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getReferenciaTransaccion() { return referenciaTransaccion; }
    public void setReferenciaTransaccion(String referenciaTransaccion) { this.referenciaTransaccion = referenciaTransaccion; }

    public List<ComprobantePago> getComprobantes() { return comprobantes; }
    public void setComprobantes(List<ComprobantePago> comprobantes) { this.comprobantes = comprobantes; }
}