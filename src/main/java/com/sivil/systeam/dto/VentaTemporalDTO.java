package com.sivil.systeam.dto;

import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.EstadoVenta;
import com.sivil.systeam.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para almacenar temporalmente los datos de venta en sesión
 * antes de confirmar el pago
 */
public class VentaTemporalDTO {

    private String numeroFactura;
    private Usuario vendedor;
    private String nombreCliente;
    private String contactoCliente;
    private String identificacionCliente;
    private BigDecimal subtotal;
    private BigDecimal descuentoAplicado;
    private BigDecimal impuestos;
    private BigDecimal total;
    private MetodoPago tipoPago;
    private EstadoVenta estado;
    private LocalDateTime fechaVenta;
    private List<DetalleVentaTemporalDTO> detallesVenta;

    // Constructores
    public VentaTemporalDTO() {}

    // Getters y Setters
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getContactoCliente() { return contactoCliente; }
    public void setContactoCliente(String contactoCliente) { this.contactoCliente = contactoCliente; }

    public String getIdentificacionCliente() { return identificacionCliente; }
    public void setIdentificacionCliente(String identificacionCliente) { this.identificacionCliente = identificacionCliente; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(BigDecimal descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }

    public BigDecimal getImpuestos() { return impuestos; }
    public void setImpuestos(BigDecimal impuestos) { this.impuestos = impuestos; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public MetodoPago getTipoPago() { return tipoPago; }
    public void setTipoPago(MetodoPago tipoPago) { this.tipoPago = tipoPago; }

    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public List<DetalleVentaTemporalDTO> getDetallesVenta() { return detallesVenta; }
    public void setDetallesVenta(List<DetalleVentaTemporalDTO> detallesVenta) { this.detallesVenta = detallesVenta; }

    /**
     * DTO para los detalles de venta temporales
     */
    public static class DetalleVentaTemporalDTO {
        private Integer idLibro;
        private String tituloLibro;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotalItem;

        // Constructores
        public DetalleVentaTemporalDTO() {}

        public DetalleVentaTemporalDTO(Integer idLibro, String tituloLibro, Integer cantidad,
                                     BigDecimal precioUnitario, BigDecimal subtotalItem) {
            this.idLibro = idLibro;
            this.tituloLibro = tituloLibro;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotalItem = subtotalItem;
        }

        // Getters y Setters
        public Integer getIdLibro() { return idLibro; }
        public void setIdLibro(Integer idLibro) { this.idLibro = idLibro; }

        public String getTituloLibro() { return tituloLibro; }
        public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }

        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

        public BigDecimal getSubtotalItem() { return subtotalItem; }
        public void setSubtotalItem(BigDecimal subtotalItem) { this.subtotalItem = subtotalItem; }
    }
}