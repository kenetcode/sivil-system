package com.sivil.systeam.service;

import com.sivil.systeam.entity.ComprobantePago;
import com.sivil.systeam.entity.Pago;
import com.sivil.systeam.repository.ComprobantePagoRepository;
import com.sivil.systeam.repository.PagoRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class ComprobantePagoService {

    private final ComprobantePagoRepository comprobanteRepo;
    private final PagoRepository pagoRepo;

    private final String uploadDir = "uploads/comprobantes/";

    public ComprobantePagoService(ComprobantePagoRepository comprobanteRepo, PagoRepository pagoRepo) {
        this.comprobanteRepo = comprobanteRepo;
        this.pagoRepo = pagoRepo;
    }

    // Guardar comprobante PDF
    public ComprobantePago guardarComprobante(Integer pagoId, MultipartFile archivo) throws IOException {
        // Validar tipo (si tu frontend no envía contentType confiable, puedes validar por extensión)
        if (archivo.getContentType() == null || !"application/pdf".equals(archivo.getContentType())) {
            throw new IllegalArgumentException("El archivo debe ser un PDF válido.");
        }

        byte[] bytes = archivo.getBytes();
        if (bytes.length == 0) {
            throw new IllegalArgumentException("El archivo PDF está vacío o corrupto.");
        }

        Pago pago = pagoRepo.findById(pagoId)
                .orElseThrow(() -> new IllegalArgumentException("El pago no existe."));

        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio de uploads: " + uploadDir);
        }

        String nombreArchivo = archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "comprobante.pdf";
        String ruta = uploadDir + "comprobante_" + pagoId + "_" + System.currentTimeMillis() + ".pdf";

        Path path = Paths.get(ruta);
        Files.write(path, bytes, StandardOpenOption.CREATE);

        ComprobantePago comprobante = new ComprobantePago();
        comprobante.setPago(pago);
        comprobante.setNombre_archivo(nombreArchivo);
        comprobante.setRuta_archivo(ruta);
        comprobante.setTipo_archivo("PDF");
        comprobante.setTamaño_archivo(bytes.length);

        return comprobanteRepo.save(comprobante);
    }

    // Listar comprobantes por vendedor
    public List<ComprobantePago> listarPorVendedor(Integer Vendedor_Id) {
        return comprobanteRepo.findByPagoVentaVendedor_Id(Vendedor_Id);
    }

    // Obtener archivo comprobante (ver o descargar) - implementación sin lambdas para evitar inferencias
    public ResponseEntity<Resource> obtenerArchivoComprobante(Integer idComprobante, boolean descargar) {
        Optional<ComprobantePago> opt = comprobanteRepo.findById(idComprobante);

        if (opt.isEmpty()) {
            return ResponseEntity.<Resource>status(HttpStatus.NOT_FOUND).build();
        }

        ComprobantePago comprobante = opt.get();

        File archivo = new File(comprobante.getRuta_archivo());
        if (!archivo.exists() || !archivo.canRead()) {
            return ResponseEntity.<Resource>status(HttpStatus.NOT_FOUND).build();
        }

        Resource resource = new FileSystemResource(archivo);

        HttpHeaders headers = new HttpHeaders();
        String disposition = (descargar ? "attachment" : "inline") + "; filename=\"" + comprobante.getNombre_archivo() + "\"";
        headers.add(HttpHeaders.CONTENT_DISPOSITION, disposition);

        // Forzar tipo PDF (opcional, pero recomendado)
        MediaType mediaType = MediaType.APPLICATION_PDF;

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(resource);
    }
}