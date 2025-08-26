package com.sivil.systeam.entity;

import com.sivil.systeam.enums.Estado;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_libro;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo_libro;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String autor;

    private Integer año_publicacion;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal precio;

    private Integer cantidad_stock = 0;

    @Column(length = 50)
    private String categoria;

    @Column(length = 100)
    private String editorial;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 500)
    private String imagen_url;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.activo;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_creacion;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_ultima_actualizacion;

    // Relaciones
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detallesVenta;

    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleCompra> detallesCompra;

    // Constructores
    public Libro() {}

    public Libro(String codigo_libro, String titulo, String autor,
                 Integer año_publicacion, BigDecimal precio) {
        this.codigo_libro = codigo_libro;
        this.titulo = titulo;
        this.autor = autor;
        this.año_publicacion = año_publicacion;
        this.precio = precio;
    }

    // Getters y Setters
    public Integer getId_libro() { return id_libro; }
    public void setId_libro(Integer id_libro) { this.id_libro = id_libro; }

    public String getCodigo_libro() { return codigo_libro; }
    public void setCodigo_libro(String codigo_libro) { this.codigo_libro = codigo_libro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public Integer getAño_publicacion() { return año_publicacion; }
    public void setAño_publicacion(Integer año_publicacion) { this.año_publicacion = año_publicacion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getCantidad_stock() { return cantidad_stock; }
    public void setCantidad_stock(Integer cantidad_stock) { this.cantidad_stock = cantidad_stock; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen_url() { return imagen_url; }
    public void setImagen_url(String imagen_url) { this.imagen_url = imagen_url; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(LocalDateTime fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public LocalDateTime getFecha_ultima_actualizacion() { return fecha_ultima_actualizacion; }
    public void setFecha_ultima_actualizacion(LocalDateTime fecha_ultima_actualizacion) { this.fecha_ultima_actualizacion = fecha_ultima_actualizacion; }

    public List<DetalleVenta> getDetallesVenta() { return detallesVenta; }
    public void setDetallesVenta(List<DetalleVenta> detallesVenta) { this.detallesVenta = detallesVenta; }

    public List<DetalleCompra> getDetallesCompra() { return detallesCompra; }
    public void setDetallesCompra(List<DetalleCompra> detallesCompra) { this.detallesCompra = detallesCompra; }
}