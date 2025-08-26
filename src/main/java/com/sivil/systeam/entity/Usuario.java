package com.sivil.systeam.entity;

import com.sivil.systeam.enums.Estado;
import com.sivil.systeam.enums.TipoUsuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_usuario;

    @Column(unique = true, nullable = false, length = 50)
    private String nombre_usuario;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String contraseña;

    @Column(nullable = false, length = 150)
    private String nombre_completo;

    @Column(length = 15)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipo_usuario;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.activo;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_creacion;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha_ultima_actualizacion;

    // Relaciones
    @OneToMany(mappedBy = "vendedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Venta> ventas;

    @OneToMany(mappedBy = "comprador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CompraOnline> compras;

    // Constructores
    public Usuario() {}

    public Usuario(String nombre_usuario, String email, String contraseña,
                   String nombre_completo, TipoUsuario tipo_usuario) {
        this.nombre_usuario = nombre_usuario;
        this.email = email;
        this.contraseña = contraseña;
        this.nombre_completo = nombre_completo;
        this.tipo_usuario = tipo_usuario;
    }

    // Getters y Setters
    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id_usuario) { this.id_usuario = id_usuario; }

    public String getNombre_usuario() { return nombre_usuario; }
    public void setNombre_usuario(String nombre_usuario) { this.nombre_usuario = nombre_usuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getNombre_completo() { return nombre_completo; }
    public void setNombre_completo(String nombre_completo) { this.nombre_completo = nombre_completo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public TipoUsuario getTipo_usuario() { return tipo_usuario; }
    public void setTipo_usuario(TipoUsuario tipo_usuario) { this.tipo_usuario = tipo_usuario; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(LocalDateTime fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public LocalDateTime getFecha_ultima_actualizacion() { return fecha_ultima_actualizacion; }
    public void setFecha_ultima_actualizacion(LocalDateTime fecha_ultima_actualizacion) { this.fecha_ultima_actualizacion = fecha_ultima_actualizacion; }

    public List<Venta> getVentas() { return ventas; }
    public void setVentas(List<Venta> ventas) { this.ventas = ventas; }

    public List<CompraOnline> getCompras() { return compras; }
    public void setCompras(List<CompraOnline> compras) { this.compras = compras; }
}