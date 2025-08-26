package com.sivil.systeam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes_pago")
public class ComprobantePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer idComprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comprobantes_pago"))
    @NotNull
    private Pago pago;

    @Column(name = "nombre_archivo", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    @NotBlank
    @Size(max = 500)
    private String rutaArchivo;

    @Column(name = "tipo_archivo", length = 10)
    @Size(max = 10)
    private String tipoArchivo = "PDF";

    @Column(name = "tamaño_archivo")
    private Integer tamañoArchivo;

    @CreationTimestamp
    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    // Constructores
    public ComprobantePago() {}

    public ComprobantePago(Pago pago, String nombreArchivo, String rutaArchivo) {
        this.pago = pago;
        this.nombreArchivo = nombreArchivo;
        this.rutaArchivo = rutaArchivo;
    }

    // Getters y Setters
    public Integer getIdComprobante() { return idComprobante; }
    public void setIdComprobante(Integer idComprobante) { this.idComprobante = idComprobante; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) { this.pago = pago; }

    public String getNombreArchivo() { return nombreArchivo; }
    public void setNombreArchivo(String nombreArchivo) { this.nombreArchivo = nombreArchivo; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getTipoArchivo() { return tipoArchivo; }
    public void setTipoArchivo(String tipoArchivo) { this.tipoArchivo = tipoArchivo; }

    public Integer getTamañoArchivo() { return tamañoArchivo; }
    public void setTamañoArchivo(Integer tamañoArchivo) { this.tamañoArchivo = tamañoArchivo; }

    public LocalDateTime getFechaSubida() { return fechaSubida; }
    public void setFechaSubida(LocalDateTime fechaSubida) { this.fechaSubida = fechaSubida; }
}