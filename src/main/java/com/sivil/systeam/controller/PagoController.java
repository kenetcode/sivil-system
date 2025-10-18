package com.sivil.systeam.controller;

import com.sivil.systeam.dto.CompraTemporalDTO;
import com.sivil.systeam.dto.VentaTemporalDTO;
import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.service.PagoService;
import com.sivil.systeam.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/pago")
public class PagoController {

    private final PagoService pagoService;
    private final UsuarioService usuarioService;

    public PagoController(PagoService pagoService, UsuarioService usuarioService) {
        this.pagoService = pagoService;
        this.usuarioService = usuarioService;
    }

    /* ==============================
     * TARJETA (lo que ya tenías)
     * ============================== */

    @GetMapping("/pago-tarjeta")
    public String mostrarPagoTarjeta(Model model,
                                     @RequestParam(value = "monto", required = false) BigDecimal monto,
                                     @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                     @RequestParam(value = "idVenta", required = false) Integer idVenta) {
        model.addAttribute("pago", new Pago());
        if (monto != null) model.addAttribute("montoAPagar", monto);
        if (idCompra != null) model.addAttribute("idCompra", idCompra);
        if (idVenta != null) model.addAttribute("idVenta", idVenta);
        return "pago/pago-tarjeta";
    }

    @GetMapping("/tarjeta")
    public String mostrarFormularioTarjeta(Model model,
                                           @RequestParam(value = "monto", required = false) BigDecimal monto,
                                           @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                           @RequestParam(value = "idVenta", required = false) Integer idVenta,
                                           @RequestParam(value = "ventaPendiente", required = false) Boolean ventaPendiente,
                                           @RequestParam(value = "compraPendiente", required = false) Boolean compraPendiente,
                                           HttpSession session) {
        model.addAttribute("pago", new Pago());
        if (monto != null) model.addAttribute("montoAPagar", monto);
        if (idCompra != null) model.addAttribute("idCompra", idCompra);
        if (idVenta != null) model.addAttribute("idVenta", idVenta);
        if (Boolean.TRUE.equals(ventaPendiente)) model.addAttribute("ventaPendiente", true);
        if (Boolean.TRUE.equals(compraPendiente)) model.addAttribute("compraPendiente", true);
        return "pago/pago-tarjeta";
    }

    @PostMapping("/procesar")
    public String procesarPagoTarjeta(@RequestParam("numeroTarjeta") String numeroTarjeta,
                                      @RequestParam("fechaVencimiento") String fechaVencimiento,
                                      @RequestParam("cvv") String cvv,
                                      @RequestParam("nombreTitular") String nombreTitular,
                                      @RequestParam("email") String email,
                                      @RequestParam("direccion") String direccion,
                                      @RequestParam(value = "monto", required = false, defaultValue = "100.00") BigDecimal monto,
                                      @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                      @RequestParam(value = "idVenta", required = false) Integer idVenta,
                                      @RequestParam(value = "ventaPendiente", required = false) Boolean ventaPendiente,
                                      @RequestParam(value = "compraPendiente", required = false) Boolean compraPendiente,
                                      HttpSession session,
                                      Model model) {
        try {
            Pago pago = new Pago();
            pago.setMonto(monto);

            if (Boolean.TRUE.equals(ventaPendiente)) {
                VentaTemporalDTO ventaTemporal = (VentaTemporalDTO) session.getAttribute("ventaPendiente");
                if (ventaTemporal == null) {
                    model.addAttribute("error", "La sesión de venta ha expirado. Por favor inicie la venta nuevamente.");
                    return "pago/pago-tarjeta";
                }
                Pago pagoProcesado = pagoService.procesarPagoConVentaPendiente(
                        numeroTarjeta, fechaVencimiento, cvv,
                        nombreTitular, email, direccion, pago, ventaTemporal);
                session.removeAttribute("ventaPendiente");
                model.addAttribute("mensaje", "✅ Pago procesado correctamente - Venta creada");
                model.addAttribute("pago", pagoProcesado);
                model.addAttribute("numeroTarjetaOculto", "****-****-****-" + numeroTarjeta.substring(numeroTarjeta.length() - 4));
                return "pago/pago-confirmacion";

            } else if (Boolean.TRUE.equals(compraPendiente)) {
                CompraTemporalDTO compraTemporal = (CompraTemporalDTO) session.getAttribute("compraPendiente");
                if (compraTemporal == null) {
                    model.addAttribute("error", "La sesión de compra ha expirado. Por favor inicie la compra nuevamente.");
                    return "pago/pago-tarjeta";
                }
                Pago pagoProcesado = pagoService.procesarPagoConCompraPendiente(
                        numeroTarjeta, fechaVencimiento, cvv,
                        nombreTitular, email, direccion, pago, compraTemporal);
                session.removeAttribute("compraPendiente");
                model.addAttribute("mensaje", "✅ Compra realizada exitosamente - Orden: " + compraTemporal.getNumeroOrden());
                model.addAttribute("pago", pagoProcesado);
                model.addAttribute("numeroTarjetaOculto", "****-****-****-" + numeroTarjeta.substring(numeroTarjeta.length() - 4));
                model.addAttribute("esCompraOnline", true);
                return "pago/pago-confirmacion";

            } else {
                Pago pagoProcesado = pagoService.procesarPago(
                        numeroTarjeta, fechaVencimiento, cvv,
                        nombreTitular, email, direccion, pago, idCompra, idVenta);
                if (idCompra == null && idVenta == null) {
                    model.addAttribute("mensaje", "✅ Pago procesado correctamente");
                    model.addAttribute("esSimulacion", true);
                } else {
                    model.addAttribute("mensaje", "✅ Pago procesado correctamente");
                }
                model.addAttribute("pago", pagoProcesado);
                model.addAttribute("numeroTarjetaOculto", "****-****-****-" + numeroTarjeta.substring(numeroTarjeta.length() - 4));
                return "pago/pago-confirmacion";
            }

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pago", new Pago());
            model.addAttribute("montoAPagar", monto);
            model.addAttribute("nombreTitular", nombreTitular);
            model.addAttribute("email", email);
            model.addAttribute("direccion", direccion);
            if (Boolean.TRUE.equals(ventaPendiente)) model.addAttribute("ventaPendiente", true);
            if (Boolean.TRUE.equals(compraPendiente)) model.addAttribute("compraPendiente", true);
            return "pago/pago-tarjeta";
        }
    }

    /* ==============================
     * EFECTIVO (nuevo)
     * ============================== */

    // GET: mostrar formulario de efectivo.
    // ACEPTA idVenta (venta ya guardada) O ventaPendiente=true (venta en sesión).
    @GetMapping("/efectivo")
    public String mostrarEfectivo(Model model,
                                  @RequestParam("monto") BigDecimal monto,
                                  @RequestParam(value = "idVenta", required = false) Integer idVenta,
                                  @RequestParam(value = "ventaPendiente", required = false) Boolean ventaPendiente) {
        model.addAttribute("montoAPagar", monto);
        if (idVenta != null) model.addAttribute("idVenta", idVenta);
        if (Boolean.TRUE.equals(ventaPendiente)) model.addAttribute("ventaPendiente", true);
        return "pago/pago-efectivo-venta";
    }

    // POST: confirmar efectivo.
    @PostMapping("/efectivo/confirmar")
    public String confirmarEfectivo(HttpSession session,
                                    Model model,
                                    @RequestParam("monto") BigDecimal montoRecibido,
                                    @RequestParam(value = "idVenta", required = false) Integer idVenta,
                                    @RequestParam(value = "ventaPendiente", required = false) Boolean ventaPendiente,
                                    @RequestParam(value = "observaciones", required = false) String observaciones) {

        try {
            // Recuperar vendedor actual del sistema de autenticación
            Usuario vendedor = usuarioService.getUsuarioActual();
            String emailVendedor = vendedor != null ? vendedor.getEmail() : null;

            // 1) Venta ya existente
            if (idVenta != null) {
                Pago pago = pagoService.procesarPagoEfectivoVenta(
                        idVenta,
                        montoRecibido,
                        observaciones,
                        emailVendedor
                );
                model.addAttribute("mensaje", "✅ Pago en efectivo registrado correctamente.");
                model.addAttribute("pago", pago);
                return "pago/pago-confirmacion";
            }

            // 2) Venta temporal en sesión
            if (Boolean.TRUE.equals(ventaPendiente)) {
                VentaTemporalDTO ventaTemporal = (VentaTemporalDTO) session.getAttribute("ventaPendiente");
                if (ventaTemporal == null) {
                    model.addAttribute("error", "La sesión de venta ha expirado. Vuelve a iniciar la venta.");
                    model.addAttribute("montoAPagar", montoRecibido);
                    model.addAttribute("ventaPendiente", true);
                    return "pago/pago-efectivo-venta";
                }

                Pago pago = pagoService.procesarPagoEfectivoVentaPendiente(
                        ventaTemporal,
                        montoRecibido,
                        observaciones,
                        emailVendedor
                );

                session.removeAttribute("ventaPendiente");
                model.addAttribute("mensaje", "✅ Venta creada y pagada en efectivo correctamente.");
                model.addAttribute("pago", pago);
                return "pago/pago-confirmacion";
            }

            // Si no vino idVenta ni ventaPendiente
            model.addAttribute("error", "Falta identificar la venta (idVenta) o la venta pendiente.");
            model.addAttribute("montoAPagar", montoRecibido);
            return "pago/pago-efectivo-venta";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("montoAPagar", montoRecibido);
            if (idVenta != null) model.addAttribute("idVenta", idVenta);
            if (Boolean.TRUE.equals(ventaPendiente)) model.addAttribute("ventaPendiente", true);
            return "pago/pago-efectivo-venta";

        } catch (Exception ex) {
            model.addAttribute("error", "Ocurrió un error al registrar el pago en efectivo.");
            model.addAttribute("montoAPagar", montoRecibido);
            if (idVenta != null) model.addAttribute("idVenta", idVenta);
            if (Boolean.TRUE.equals(ventaPendiente)) model.addAttribute("ventaPendiente", true);
            return "pago/pago-efectivo-venta";
        }
    }
}
