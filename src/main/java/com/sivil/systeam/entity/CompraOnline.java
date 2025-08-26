package com.sivil.systeam.entity;

import com.sivil.systeam.enums.EstadoCompra;
import com.sivil.systeam.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compras_online")
public class CompraOnline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    @Column(name = "numero_orden", unique = true, nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String numeroOrden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador", nullable = false,
            foreignKey = @ForeignKey(name = "fk_compras_comprador"))
    @NotNull
    private Usuario comprador;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.00")
    @NotNull
    private BigDecimal subtotal;

    @Column(name = "impuestos", precision = 10, scale = 2)
    @DecimalMin(value = "0.00")
    private BigDecimal impuestos = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.00")
    @NotNull
    private BigDecimal total;

    @Column(name = "direccion_entrega", columnDefinition = "TEXT")
    private String direccionEntrega;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_compra")
    private EstadoCompra estadoCompra = EstadoCompra.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago = MetodoPago.TARJETA;

    @CreationTimestamp
    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;

    @UpdateTimestamp
    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DetalleCompra> detallesCompra;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Pago> pagos;

    // Constructores
    public CompraOnline() {}

    public CompraOnline(String numeroOrden, Usuario comprador,
                        BigDecimal subtotal, BigDecimal total) {
        this.numeroOrden = numeroOrden;
        this.comprador = comprador;
        this.subtotal = subtotal;
        this.total = total;
    }

    // Getters y Setters
    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer idCompra) { this.idCompra = idCompra; }

    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public EstadoCompra getEstadoCompra() { return estadoCompra; }
    public void setEstadoCompra(EstadoCompra estadoCompra) { this.estadoCompra = estadoCompra; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }

    public LocalDateTime getFechaUltimaActualizacion() { return fechaUltimaActualizacion; }
    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) { this.fechaUltimaActualizacion = fechaUltimaActualizacion; }

    public List<DetalleCompra> getDetallesCompra() { return detallesCompra; }
    public void setDetallesCompra(List<DetalleCompra> detallesCompra) { this.detallesCompra = detallesCompra; }

    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }
}