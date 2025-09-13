package com.sivil.systeam.entity;

import com.sivil.systeam.enums.EstadoCompra;
import com.sivil.systeam.enums.MetodoPago;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compras_online")
public class CompraOnline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_compra;

    @Column(unique = true, nullable = false, length = 50)
    private String numero_orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador", nullable = false,
            foreignKey = @ForeignKey(name = "fk_compras_comprador"))
    private Usuario comprador;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "impuestos", precision = 10, scale = 2)
    private BigDecimal impuestos = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String direccion_entrega;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado_compra = EstadoCompra.pendiente;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodo_pago = MetodoPago.tarjeta;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_compra;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_ultima_actualizacion;

    // Relaciones
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<DetalleCompra> detallesCompra;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Pago> pagos;

    // Constructores
    public CompraOnline() {}

    public CompraOnline(String numero_orden, Usuario comprador,
                        BigDecimal subtotal, BigDecimal total) {
        this.numero_orden = numero_orden;
        this.comprador = comprador;
        this.subtotal = subtotal;
        this.total = total;
    }

    // Getters y Setters
    public Integer getId_compra() { return id_compra; }
    public void setId_compra(Integer id_compra) { this.id_compra = id_compra; }

    public String getNumero_orden() { return numero_orden; }
    public void setNumero_orden(String numero_orden) { this.numero_orden = numero_orden; }

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getDireccion_entrega() { return direccion_entrega; }
    public void setDireccion_entrega(String direccion_entrega) { this.direccion_entrega = direccion_entrega; }

    public EstadoCompra getEstado_compra() { return estado_compra; }
    public void setEstado_compra(EstadoCompra estado_compra) { this.estado_compra = estado_compra; }

    public MetodoPago getMetodo_pago() { return metodo_pago; }
    public void setMetodo_pago(MetodoPago metodo_pago) { this.metodo_pago = metodo_pago; }

    public LocalDateTime getFecha_compra() { return fecha_compra; }
    public void setFecha_compra(LocalDateTime fecha_compra) { this.fecha_compra = fecha_compra; }

    public LocalDateTime getFecha_ultima_actualizacion() { return fecha_ultima_actualizacion; }
    public void setFecha_ultima_actualizacion(LocalDateTime fecha_ultima_actualizacion) { this.fecha_ultima_actualizacion = fecha_ultima_actualizacion; }

    public List<DetalleCompra> getDetallesCompra() { return detallesCompra; }
    public void setDetallesCompra(List<DetalleCompra> detallesCompra) { this.detallesCompra = detallesCompra; }

    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }

    // Método helper para calcular la cantidad total de libros
    public Integer getCantidadTotalLibros() {
        if (detallesCompra == null || detallesCompra.isEmpty()) {
            return 0;
        }
        return detallesCompra.stream()
                .mapToInt(DetalleCompra::getCantidad)
                .sum();
    }
}