package com.sivil.systeam.dto;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.EstadoCompra;
import com.sivil.systeam.enums.MetodoPago;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CompraTemporalDTO implements Serializable {

    private String numeroOrden;
    private Usuario comprador;
    private String nombreCliente;
    private String contactoCliente;
    private String identificacionCliente;
    private String direccionEntrega;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private MetodoPago metodoPago;
    private EstadoCompra estadoCompra;
    private LocalDateTime fechaCompra;
    private List<DetalleCompraTemporalDTO> detallesCompra;

    // Constructores
    public CompraTemporalDTO() {}

    // Clase interna para los detalles
    public static class DetalleCompraTemporalDTO implements Serializable {
        private Integer libroId;
        private String tituloLibro;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public DetalleCompraTemporalDTO() {}

        public DetalleCompraTemporalDTO(Integer libroId, String tituloLibro, Integer cantidad,
                                       BigDecimal precioUnitario, BigDecimal subtotal) {
            this.libroId = libroId;
            this.tituloLibro = tituloLibro;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }

        // Getters y Setters
        public Integer getLibroId() { return libroId; }
        public void setLibroId(Integer libroId) { this.libroId = libroId; }

        public String getTituloLibro() { return tituloLibro; }
        public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    // Getters y Setters
    public String getNumeroOrden() { return numeroOrden; }
    public void setNumeroOrden(String numeroOrden) { this.numeroOrden = numeroOrden; }

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getContactoCliente() { return contactoCliente; }
    public void setContactoCliente(String contactoCliente) { this.contactoCliente = contactoCliente; }

    public String getIdentificacionCliente() { return identificacionCliente; }
    public void setIdentificacionCliente(String identificacionCliente) { this.identificacionCliente = identificacionCliente; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public EstadoCompra getEstadoCompra() { return estadoCompra; }
    public void setEstadoCompra(EstadoCompra estadoCompra) { this.estadoCompra = estadoCompra; }

    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime fechaCompra) { this.fechaCompra = fechaCompra; }

    public List<DetalleCompraTemporalDTO> getDetallesCompra() { return detallesCompra; }
    public void setDetallesCompra(List<DetalleCompraTemporalDTO> detallesCompra) { this.detallesCompra = detallesCompra; }
}