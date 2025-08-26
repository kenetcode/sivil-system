package com.sivil.systeam.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes_pago")
public class ComprobantePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_comprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comprobantes_pago"))
    private Pago pago;

    @Column(nullable = false)
    private String nombre_archivo;

    @Column(nullable = false, length = 500)
    private String ruta_archivo;

    @Column(length = 10)
    private String tipo_archivo = "PDF";

    private Integer tamaño_archivo;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_subida;

    // Constructores
    public ComprobantePago() {}

    public ComprobantePago(Pago pago, String nombre_archivo, String ruta_archivo) {
        this.pago = pago;
        this.nombre_archivo = nombre_archivo;
        this.ruta_archivo = ruta_archivo;
    }

    // Getters y Setters
    public Integer getId_comprobante() { return id_comprobante; }
    public void setId_comprobante(Integer id_comprobante) { this.id_comprobante = id_comprobante; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public String getNombre_archivo() { return nombre_archivo; }
    public void setNombre_archivo(String nombre_archivo) { this.nombre_archivo = nombre_archivo; }

    public String getRuta_archivo() { return ruta_archivo; }
    public void setRuta_archivo(String ruta_archivo) { this.ruta_archivo = ruta_archivo; }

    public String getTipo_archivo() { return tipo_archivo; }
    public void setTipo_archivo(String tipo_archivo) { this.tipo_archivo = tipo_archivo; }

    public Integer getTamaño_archivo() { return tamaño_archivo; }
    public void setTamaño_archivo(Integer tamaño_archivo) { this.tamaño_archivo = tamaño_archivo; }

    public LocalDateTime getFecha_subida() { return fecha_subida; }
    public void setFecha_subida(LocalDateTime fecha_subida) { this.fecha_subida = fecha_subida; }
}