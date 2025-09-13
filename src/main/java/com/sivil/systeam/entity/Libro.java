package com.sivil.systeam.entity;

import com.sivil.systeam.enums.Estado;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad que representa un libro en el sistema SIVIL
 * Tabla: libros en PostgreSQL
 * Usada en HU013: Agregar Libro al Inventario
 */
@Entity
@Table(name = "libros")
public class Libro {

    // ============================================
    // CAMPOS DE LA BASE DE DATOS
    // ============================================

    @Id // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento en PostgreSQL
    private Integer id_libro; // Campo id_libro en la base de datos

    @Column(unique = true, nullable = false, length = 20) // Restricción de unicidad en BD
    @NotBlank(message = "El código del libro es obligatorio") // Validación: no puede estar vacío
    @Size(max = 20, message = "El código del libro no puede exceder 20 caracteres") // Máximo 20 caracteres
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "El código del libro solo puede contener caracteres alfanuméricos") // Solo letras y números
    private String codigo_libro; // Código único del libro (ej: "LIB001")

    @Column(nullable = false, length = 200) // Campo obligatorio con máximo 200 caracteres
    @NotBlank(message = "El título del libro es obligatorio") // No puede estar vacío
    @Size(max = 200, message = "El título no puede exceder 200 caracteres")
    private String titulo; // Título del libro

    @Column(nullable = false, length = 100) // Campo obligatorio con máximo 100 caracteres
    @NotBlank(message = "El autor del libro es obligatorio") // No puede estar vacío
    @Size(max = 100, message = "El nombre del autor no puede exceder 100 caracteres")
    private String autor; // Nombre del autor

    @NotNull(message = "El año de publicación es obligatorio") // Campo obligatorio
    @Min(value = 1900, message = "El año de publicación no puede ser menor a 1900") // Mínimo 1900
    @Max(value = 2025, message = "El año de publicación no puede ser mayor al año actual") // Máximo año actual
    private Integer año_publicacion; // Año de publicación del libro

    @Column(precision = 10, scale = 2, nullable = false) // Decimal con 2 decimales, obligatorio
    @NotNull(message = "El precio es obligatorio") // Campo obligatorio
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") // Debe ser positivo
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 dígitos enteros y 2 decimales")
    private BigDecimal precio; // Precio del libro

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer cantidad_stock;

    @Column(length = 50) // Máximo 50 caracteres, campo opcional
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String categoria; // Categoría del libro (ej: "Ficción", "Historia")

    @Column(length = 100) // Máximo 100 caracteres, campo opcional
    @Size(max = 100, message = "La editorial no puede exceder 100 caracteres")
    private String editorial; // Editorial que publicó el libro

    @Column(columnDefinition = "TEXT") // Campo de texto largo, opcional
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion; // Descripción detallada del libro

    @Column(length = 500) // Máximo 500 caracteres para URL, campo opcional
    @Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres")
    private String imagen_url; // URL de la imagen de portada del libro

    @Enumerated(EnumType.STRING) // Almacenar como string en BD ("activo" o "inactivo")
    private Estado estado = Estado.activo; // Estado del libro, default = activo

    // Campos de auditoría - manejados automáticamente por PostgreSQL triggers
    @Column(insertable = false, updatable = false) // No modificar desde Java
    private LocalDateTime fecha_creacion; // Fecha cuando se creó el registro

    @Column(insertable = false, updatable = false) // No modificar desde Java
    private LocalDateTime fecha_ultima_actualizacion; // Fecha de última modificación

    // ============================================
    // RELACIONES CON OTRAS ENTIDADES
    // ============================================

    // Relación uno a muchos: Un libro puede estar en múltiples detalles de venta
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleVenta> detallesVenta; // Lista de ventas donde aparece este libro

    // Relación uno a muchos: Un libro puede estar en múltiples detalles de compra
    @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleCompra> detallesCompra; // Lista de compras online donde aparece este libro

    // ============================================
    // CONSTRUCTORES
    // ============================================

    /**
     * Constructor vacío requerido por JPA
     * Establece valores por defecto
     */
    public Libro() {
        this.estado = Estado.activo; // Por defecto, los libros se crean activos
        this.cantidad_stock = 0; // Por defecto, sin stock inicial
    }

    /**
     * Constructor con campos obligatorios
     * Usado para crear libros con datos mínimos necesarios
     */
    public Libro(String codigo_libro, String titulo, String autor,
                 Integer año_publicacion, BigDecimal precio) {
        this(); // Llamar constructor vacío para establecer defaults
        this.codigo_libro = codigo_libro;
        this.titulo = titulo;
        this.autor = autor;
        this.año_publicacion = año_publicacion;
        this.precio = precio;
    }

    // ============================================
    // MÉTODOS DE NEGOCIO PARA HU013
    // ============================================

    /**
     * Verificar si el libro está activo y disponible para venta
     * @return true si el estado es "activo"
     */
    public boolean isActivo() {
        return Estado.activo.equals(this.estado);
    }

    /**
     * Verificar si hay stock disponible del libro
     * @return true si la cantidad en stock es mayor a 0
     */
    public boolean tieneStock() {
        return this.cantidad_stock != null && this.cantidad_stock > 0;
    }

    /**
     * Verificar si el stock es suficiente para una cantidad solicitada
     * @param cantidadSolicitada cantidad que se quiere comprar/reservar
     * @return true si hay suficiente stock
     */
    public boolean stockSuficiente(int cantidadSolicitada) {
        return this.cantidad_stock != null && this.cantidad_stock >= cantidadSolicitada;
    }

    /**
     * HU013: Verificar si el libro tiene stock bajo (menos de 5 unidades)
     * Usado para alertas de reabastecimiento en el inventario
     * @return true si tiene entre 1 y 4 unidades (stock crítico)
     */
    public boolean tieneStockBajo() {
        return this.cantidad_stock != null &&
                this.cantidad_stock > 0 &&
                this.cantidad_stock < 5;
    }

    /**
     * Calcular el valor total de este libro en inventario
     * @return precio * cantidad_stock
     */
    public BigDecimal calcularValorInventario() {
        if (precio == null || cantidad_stock == null) {
            return BigDecimal.ZERO; // Retornar 0 si faltan datos
        }
        return precio.multiply(BigDecimal.valueOf(cantidad_stock));
    }

    /**
     * Activar el libro (cambiar estado a activo)
     */
    public void activar() {
        this.estado = Estado.activo;
    }

    /**
     * Desactivar el libro (cambiar estado a inactivo)
     * El libro no aparecerá en búsquedas ni estará disponible para venta
     */
    public void desactivar() {
        this.estado = Estado.inactivo;
    }

    // ============================================
    // GETTERS Y SETTERS
    // Métodos para acceder y modificar los campos privados
    // ============================================

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
    public void setFecha_ultima_actualizacion(LocalDateTime fecha_ultima_actualizacion) {
        this.fecha_ultima_actualizacion = fecha_ultima_actualizacion;
    }

    public List<DetalleVenta> getDetallesVenta() { return detallesVenta; }
    public void setDetallesVenta(List<DetalleVenta> detallesVenta) { this.detallesVenta = detallesVenta; }

    public List<DetalleCompra> getDetallesCompra() { return detallesCompra; }
    public void setDetallesCompra(List<DetalleCompra> detallesCompra) { this.detallesCompra = detallesCompra; }

    // ============================================
    // MÉTODOS EQUALS, HASHCODE Y TOSTRING
    // ============================================

    /**
     * Dos libros son iguales si tienen el mismo código
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Libro libro = (Libro) o;
        return codigo_libro != null ? codigo_libro.equals(libro.codigo_libro) : libro.codigo_libro == null;
    }

    /**
     * Hash basado en el código del libro (único)
     */
    @Override
    public int hashCode() {
        return codigo_libro != null ? codigo_libro.hashCode() : 0;
    }

    /**
     * Representación en string del libro para debugging
     */
    @Override
    public String toString() {
        return "Libro{" +
                "id_libro=" + id_libro +
                ", codigo_libro='" + codigo_libro + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", año_publicacion=" + año_publicacion +
                ", precio=" + precio +
                ", cantidad_stock=" + cantidad_stock +
                ", categoria='" + categoria + '\'' +
                ", estado=" + estado +
                '}';
    }
}