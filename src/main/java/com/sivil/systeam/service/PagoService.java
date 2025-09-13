package com.sivil.systeam.service;

import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.entity.Venta;
import com.sivil.systeam.entity.Usuario;
import com.sivil.systeam.enums.EstadoPago;
import com.sivil.systeam.enums.EstadoVenta;
import com.sivil.systeam.enums.MetodoPago;
import com.sivil.systeam.repository.PagoRepository;
import com.sivil.systeam.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PagoService {

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;

    private static final Pattern NUMERO_TARJETA_PATTERN = Pattern.compile("\\d{16}");
    private static final Pattern FECHA_VENCIMIENTO_PATTERN = Pattern.compile("(0[1-9]|1[0-2])/\\d{2}");
    private static final Pattern CVV_PATTERN = Pattern.compile("\\d{3}");

    public PagoService(PagoRepository pagoRepository, VentaRepository ventaRepository) {
        this.pagoRepository = pagoRepository;
        this.ventaRepository = ventaRepository;
    }

    public Pago procesarPago(String numeroTarjeta, String fechaVencimiento, String cvv, 
                           String nombreTitular, String email, String direccion, 
                           Pago pago, Integer idCompra, Integer idVenta) {
        
        // Validar formato de datos de tarjeta
        validarDatosTarjeta(numeroTarjeta, fechaVencimiento, cvv, nombreTitular, email);
        
        // Simular datos encriptados (solo para demostración)
        String datosSimulados = simularEncriptacion(numeroTarjeta, fechaVencimiento, cvv);
        pago.setDatos_tarjeta_encriptados(datosSimulados);
        
        // Configurar pago
        pago.setMetodo_pago(MetodoPago.tarjeta);
        pago.setEstado_pago(EstadoPago.completado);
        pago.setReferencia_transaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        // Asociar el pago con la venta o compra correspondiente
        if (idVenta != null) {
            // Buscar la venta y asociarla al pago
            Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + idVenta));
            pago.setVenta(venta);
        } else if (idCompra != null) {
            // TODO: Implementar lógica para compras cuando sea necesario
            throw new IllegalArgumentException("Procesamiento de compras no implementado aún");
        } else {
            // Si no viene ni venta ni compra, es un pago independiente (no permitido por el constraint)
            throw new IllegalArgumentException("Debe especificar una venta o compra para procesar el pago (La vista [pago con tarjeta] desde esta pestaña [Gestión de Pagos] es solo demostrativa, para ver su funcionalidad real, inicie el proceso de venta en la pestaña [Realizar Venta] o el proceso de compra desde [Compras Online])");
        }

        return pagoRepository.save(pago);
    }
    
    private void validarDatosTarjeta(String numeroTarjeta, String fechaVencimiento, String cvv, 
                                   String nombreTitular, String email) {
        
        if (numeroTarjeta == null || !NUMERO_TARJETA_PATTERN.matcher(numeroTarjeta.replaceAll("\\s+", "")).matches()) {
            throw new IllegalArgumentException("El número de tarjeta debe tener exactamente 16 dígitos");
        }
        
        if (fechaVencimiento == null || !FECHA_VENCIMIENTO_PATTERN.matcher(fechaVencimiento).matches()) {
            throw new IllegalArgumentException("La fecha de vencimiento debe tener el formato MM/AA (ejemplo: 12/25)");
        }
        
        // Validar rango de mes y año
        String[] partesFecha = fechaVencimiento.split("/");
        int mes = Integer.parseInt(partesFecha[0]);
        int año = Integer.parseInt("20" + partesFecha[1]); // Convertir AA a 20AA
        
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 01 y 12");
        }
        
        int añoActual = LocalDate.now().getYear();
        int mesActual = LocalDate.now().getMonthValue();
        
        // Validar que no sea una fecha pasada
        // Las tarjetas vencen al final del mes indicado
        if (año < añoActual || (año == añoActual && mes < mesActual)) {
            throw new IllegalArgumentException("La tarjeta no puede estar vencida");
        }
        
        if (año > añoActual + 2) {
            throw new IllegalArgumentException("Fecha de vencimiento muy lejana en el futuro");
        }
        
        if (cvv == null || !CVV_PATTERN.matcher(cvv).matches()) {
            throw new IllegalArgumentException("El CVV debe tener exactamente 3 dígitos");
        }
        
        if (nombreTitular == null || nombreTitular.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre del titular es requerido (mínimo 2 caracteres)");
        }
        
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("El email no tiene un formato válido");
        }
    }
    
    private String simularEncriptacion(String numeroTarjeta, String fechaVencimiento, String cvv) {
        // En una implementación real, aquí habría encriptación real
        // Por ahora solo guardamos los últimos 4 dígitos y ocultamos el resto
        String ultimosCuatroDigitos = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        return "****-****-****-" + ultimosCuatroDigitos + "|" + fechaVencimiento + "|***";
    }
}
