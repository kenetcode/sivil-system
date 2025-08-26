package com.sivil.systeam.entity;

import com.sivil.systeam.enums.Estado;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Integer idLibro;

    @Column(name = "codigo_libro", unique = true, nullable = false, length = 20)
    @NotBlank
    @Size(max = 20)
    private String codigoLibro;

    @Column(name = "titulo", nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String titulo;

    @Column(name = "autor", nullable = false, length = 100)
    @NotBlank
    @Size(max = 100)
    private String autor;

    @Column(name = "año_publicacion")
    @Min(1900)
    @Max(2024) // Se puede actualizar según el año actual
    private Integer añoPublicacion;

    @Column(name = "precio", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    @NotNull
    private BigDecimal precio;

    @Column(name = "cantidad_stock")
    @Min(0)
    private Integer cantidadStock = 0;

    @Column(name = "categoria", length = 50)
    @Size(max = 50)
    private String categoria;

    @Column(name = "editorial", length = 100)
    @Size(max = 100)
    private String editorial;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    @Size(max = 500)
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private Estado estado = Estado.ACTIVO;

    @CreationTimestamp
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detallesVenta;

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleCompra> detallesCompra;

    // Constructores
    public Libro() {}

    public Libro(String codigoLibro, String titulo, String autor,
                 Integer añoPublicacion, BigDecimal precio) {
        this.codigoLibro = codigoLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.añoPublicacion = añoPublicacion;
        this.precio = precio;
    }

    // Getters y Setters
    public Integer getIdLibro() { return idLibro; }
    public void setIdLibro(Integer idLibro) { this.idLibro = idLibro; }

    public String getCodigoLibro() { return codigoLibro; }
    public void setCodigoLibro(String codigoLibro) { this.codigoLibro = codigoLibro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public Integer getAñoPublicacion() { return añoPublicacion; }
    public void setAñoPublicacion(Integer añoPublicacion) { this.añoPublicacion = añoPublicacion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getCantidadStock() { return cantidadStock; }
    public void setCantidadStock(Integer cantidadStock) { this.cantidadStock = cantidadStock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaUltimaActualizacion() { return fechaUltimaActualizacion; }
    public void setFechaUltimaActualizacion(LocalDateTime fechaUltimaActualizacion) { this.fechaUltimaActualizacion = fechaUltimaActualizacion; }

    public List<DetalleVenta> getDetallesVenta() { return detallesVenta; }
    public void setDetallesVenta(List<DetalleVenta> detallesVenta) { this.detallesVenta = detallesVenta; }

    public List<DetalleCompra> getDetallesCompra() { return detallesCompra; }
    public void setDetallesCompra(List<DetalleCompra> detallesCompra) { this.detallesCompra = detallesCompra; }
}