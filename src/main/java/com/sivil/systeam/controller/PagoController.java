package com.sivil.systeam.controller;

import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.service.PagoService;
import com.sivil.systeam.dto.VentaTemporalDTO;
import com.sivil.systeam.dto.CompraTemporalDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

@Controller
@RequestMapping("/pago")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping("/seleccion-metodo-pago")
    public String mostrarSeleccionMetodoPago(Model model, 
                                           @RequestParam(value = "monto", required = false) BigDecimal monto,
                                           @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                           @RequestParam(value = "idVenta", required = false) Integer idVenta) {
        // Pasar parámetros a la vista para mantenerlos en el flujo
        if (monto != null) {
            model.addAttribute("montoAPagar", monto);
        }
        if (idCompra != null) {
            model.addAttribute("idCompra", idCompra);
        }
        if (idVenta != null) {
            model.addAttribute("idVenta", idVenta);
        }
        
        return "pago/seleccion-metodo-pago";
    }

    @GetMapping("/pago-tarjeta")
    public String mostrarPagoTarjeta(Model model, 
                                   @RequestParam(value = "monto", required = false) BigDecimal monto,
                                   @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                   @RequestParam(value = "idVenta", required = false) Integer idVenta) {
        model.addAttribute("pago", new Pago());
        
        // Si viene un monto desde otra vista, lo usamos
        if (monto != null) {
            model.addAttribute("montoAPagar", monto);
        }
        if (idCompra != null) {
            model.addAttribute("idCompra", idCompra);
        }
        if (idVenta != null) {
            model.addAttribute("idVenta", idVenta);
        }
        
        return "pago/pago-tarjeta";
    }

    @GetMapping("/tarjeta")
    public String mostrarFormulario(Model model,
                                  @RequestParam(value = "monto", required = false) BigDecimal monto,
                                  @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                  @RequestParam(value = "idVenta", required = false) Integer idVenta,
                                  @RequestParam(value = "ventaPendiente", required = false) Boolean ventaPendiente,
                                  @RequestParam(value = "compraPendiente", required = false) Boolean compraPendiente,
                                  HttpSession session) {
        model.addAttribute("pago", new Pago());

        // Si viene un monto desde otra vista, lo usamos
        if (monto != null) {
            model.addAttribute("montoAPagar", monto);
        }
        if (idCompra != null) {
            model.addAttribute("idCompra", idCompra);
        }
        if (idVenta != null) {
            model.addAttribute("idVenta", idVenta);
        }
        if (ventaPendiente != null && ventaPendiente) {
            model.addAttribute("ventaPendiente", true);
        }
        if (compraPendiente != null && compraPendiente) {
            model.addAttribute("compraPendiente", true);
        }

        return "pago/pago-tarjeta";
    }

    @PostMapping("/procesar")
    public String procesarPago(@RequestParam("numeroTarjeta") String numeroTarjeta,
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
            // Crear objeto Pago
            Pago pago = new Pago();
            pago.setMonto(monto);

            // Verificar si hay una venta pendiente en sesión
            if (ventaPendiente != null && ventaPendiente) {
                VentaTemporalDTO ventaTemporal = (VentaTemporalDTO) session.getAttribute("ventaPendiente");
                if (ventaTemporal == null) {
                    model.addAttribute("error", "La sesión de venta ha expirado. Por favor inicie el proceso de venta nuevamente.");
                    return "pago/pago-tarjeta";
                }

                // Procesar pago y crear venta
                Pago pagoProcesado = pagoService.procesarPagoConVentaPendiente(
                    numeroTarjeta, fechaVencimiento, cvv,
                    nombreTitular, email, direccion, pago, ventaTemporal);

                // Limpiar la venta temporal de la sesión
                session.removeAttribute("ventaPendiente");

                model.addAttribute("mensaje", "✅ Pago procesado correctamente - Venta creada");
                model.addAttribute("pago", pagoProcesado);
                model.addAttribute("numeroTarjetaOculto", "****-****-****-" + numeroTarjeta.substring(numeroTarjeta.length() - 4));

                return "pago/pago-confirmacion";

            // Verificar si hay una compra pendiente en sesión
            } else if (compraPendiente != null && compraPendiente) {
                CompraTemporalDTO compraTemporal = (CompraTemporalDTO) session.getAttribute("compraPendiente");
                if (compraTemporal == null) {
                    model.addAttribute("error", "La sesión de compra ha expirado. Por favor inicie el proceso de compra nuevamente.");
                    return "pago/pago-tarjeta";
                }


                // Procesar pago y crear compra online
                Pago pagoProcesado = pagoService.procesarPagoConCompraPendiente(
                    numeroTarjeta, fechaVencimiento, cvv,
                    nombreTitular, email, direccion, pago, compraTemporal);

                // Limpiar la compra temporal de la sesión
                session.removeAttribute("compraPendiente");

                model.addAttribute("mensaje", "✅ Compra realizada exitosamente - Orden: " + compraTemporal.getNumeroOrden());
                model.addAttribute("pago", pagoProcesado);
                model.addAttribute("numeroTarjetaOculto", "****-****-****-" + numeroTarjeta.substring(numeroTarjeta.length() - 4));
                model.addAttribute("esCompraOnline", true);

                return "pago/pago-confirmacion";

            } else {
                // Flujo original para pagos con ventas ya existentes
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

            // Mantener datos del formulario (excepto datos sensibles)
            model.addAttribute("nombreTitular", nombreTitular);
            model.addAttribute("email", email);
            model.addAttribute("direccion", direccion);

            // Mantener el parámetro de venta pendiente si existe
            if (ventaPendiente != null && ventaPendiente) {
                model.addAttribute("ventaPendiente", true);
            }
            // Mantener el parámetro de compra pendiente si existe
            if (compraPendiente != null && compraPendiente) {
                model.addAttribute("compraPendiente", true);
            }

            return "pago/pago-tarjeta";
        }
    }

    @PostMapping("/procesar-seleccion")
    public String procesarSeleccionMetodoPago(@RequestParam("metodoPago") String metodoPago,
                                            @RequestParam(value = "monto", required = false) BigDecimal monto,
                                            @RequestParam(value = "idCompra", required = false) Integer idCompra,
                                            @RequestParam(value = "idVenta", required = false) Integer idVenta,
                                            Model model) {
        
        // Redirigir según el método seleccionado
        if ("tarjeta".equals(metodoPago)) {
            String redirect = "redirect:/pago/pago-tarjeta";
            if (monto != null || idCompra != null || idVenta != null) {
                redirect += "?";
                boolean hasParam = false;
                if (monto != null) {
                    redirect += "monto=" + monto;
                    hasParam = true;
                }
                if (idCompra != null) {
                    redirect += (hasParam ? "&" : "") + "idCompra=" + idCompra;
                    hasParam = true;
                }
                if (idVenta != null) {
                    redirect += (hasParam ? "&" : "") + "idVenta=" + idVenta;
                }
            }
            return redirect;
        } else if ("efectivo".equals(metodoPago)) {
            // Por ahora redirigir de vuelta con mensaje
            model.addAttribute("mensaje", "Funcionalidad de pago en efectivo en desarrollo");
            return "pago/seleccion-metodo-pago";
        }
        
        // Si no se reconoce el método, volver a la selección
        return "redirect:/pago/seleccion-metodo-pago";
    }
}