package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.ConfirmarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.GenerarActaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ActaEvaluacionResponseDTO;
import org.uteq.sgacfinal.entity.ActaEvaluacion;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.ActaEvaluacionRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.IActaEvaluacionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.service.IFirmaDigitalService;
@Service
@RequiredArgsConstructor
@Transactional
public class ActaEvaluacionServiceImpl implements IActaEvaluacionService {

    private final ActaEvaluacionRepository actaRepo;
    private final PostulacionRepository postulacionRepo;
    private final org.uteq.sgacfinal.service.IPdfGeneratorService pdfService;
    private final org.uteq.sgacfinal.service.IUploadService uploadService;
    private final IFirmaDigitalService firmaDigitalService;
    private final ObjectMapper objectMapper;

    @Value("${app.file.upload-dir}")
    private String baseUploadDir;

    @Value("${firmadigital.mode:PROD}")
    private String digitalMode;

    @Override
    public ActaEvaluacionResponseDTO generarActa(GenerarActaRequestDTO request) {
        Postulacion postulacion = postulacionRepo.findById(request.getIdPostulacion())
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada: " + request.getIdPostulacion()));

        byte[] pdfBytes;
        if ("MERITOS".equalsIgnoreCase(request.getTipoActa())) {
            pdfBytes = pdfService.generarActaMeritos(postulacion);
        } else if ("OPOSICION".equalsIgnoreCase(request.getTipoActa())) {
            pdfBytes = pdfService.generarActaOposicion(postulacion);
        } else {
            throw new IllegalArgumentException("Tipo de acta inválido: " + request.getTipoActa());
        }

        String fileName = "Acta_" + request.getTipoActa() + "_" + postulacion.getEstudiante().getUsuario().getCedula() + ".pdf";
        java.util.Map<String, Object> uploadResult = uploadService.upload(pdfBytes, fileName, "application/pdf");
        String urlDocumento = uploadResult.get("url").toString();

        Integer resultado;
        var existente = actaRepo.findByIdPostulacionAndTipoActa(request.getIdPostulacion(), request.getTipoActa());
        if (existente.isPresent()) {
            resultado = actaRepo.actualizarActa(existente.get().getIdActa(), "PENDIENTE", urlDocumento);
        } else {
            resultado = actaRepo.crearActa(request.getIdPostulacion(), request.getTipoActa(), "PENDIENTE", urlDocumento);
        }

        if (resultado == null || resultado == -1) {
            throw new RuntimeException("Error al guardar o actualizar el acta de evaluación.");
        }

        ActaEvaluacion saved = actaRepo.findById(resultado)
                .orElseThrow(() -> new RuntimeException("No se encontró el acta creada/actualizada"));
        return mapToDTO(saved);
    }

    @Override
    public void eliminar(Integer idActa) {
        Integer res = actaRepo.eliminarActa(idActa);
        if (res == -1 || res == 0) {
            throw new RuntimeException("Error al eliminar el acta de evaluación o no existe.");
        }
    }

    @Override
    public ActaEvaluacionResponseDTO confirmarActa(ConfirmarActaRequestDTO request) {
        actaRepo.findById(request.getIdActa())
                .orElseThrow(() -> new RuntimeException("Acta no encontrada: " + request.getIdActa()));

        // El SP actualiza la confirmación y cambia estado a CONFIRMADO si los 3 confirmaron
        Integer resultado = actaRepo.confirmarActa(request.getIdActa(), request.getIdEvaluador(), request.getRolEvaluador());

        if (resultado == null) {
            throw new RuntimeException("Error al confirmar el acta.");
        }

        ActaEvaluacion updated = actaRepo.findById(request.getIdActa())
                .orElseThrow(() -> new RuntimeException("Acta no encontrada después de confirmar."));

        return mapToDTO(updated);
    }

    @Override
    public ActaEvaluacionResponseDTO firmarActa(Integer idActa, MultipartFile archivoFirma, String password, String rolFirmante) {
        // 1. Obtener la información del Acta actual
        ActaEvaluacion acta = actaRepo.findById(idActa)
                .orElseThrow(() -> new RuntimeException("Acta no encontrada"));

        try {
            // Obtener la ruta del archivo físico
            String fileUrl = acta.getUrlDocumento();
            String nombreArchivo = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(baseUploadDir).resolve(nombreArchivo).normalize();
            
            // 2. Preparar el PDF base
            byte[] pdfBytes;
            if ("DEV".equalsIgnoreCase(digitalMode)) {
                // En modo DEV, regeneramos un acta limpia para evitar "firmas encimadas"
                if ("MERITOS".equalsIgnoreCase(acta.getTipoActa())) {
                    pdfBytes = pdfService.generarActaMeritos(acta.getPostulacion());
                } else {
                    pdfBytes = pdfService.generarActaOposicion(acta.getPostulacion());
                }
                
                // Convertir a Base64 para el flujo de firmas
                String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
                
                // Aplicar sellos de otros evaluadores que ya confirmaron
                if (acta.getConfirmadoDecano() && !"DECANO".equalsIgnoreCase(rolFirmante)) {
                    String res = firmaDigitalService.firmarDocumentoApp("SIMA", "123", pdfBase64, "DECANO");
                    pdfBase64 = objectMapper.readTree(res).get("documento").asText();
                }
                if (acta.getConfirmadoCoordinador() && !"COORDINADOR".equalsIgnoreCase(rolFirmante)) {
                    String res = firmaDigitalService.firmarDocumentoApp("SIMA", "123", pdfBase64, "COORDINADOR");
                    pdfBase64 = objectMapper.readTree(res).get("documento").asText();
                }
                if (acta.getConfirmadoDocente() && !"DOCENTE".equalsIgnoreCase(rolFirmante)) {
                    String res = firmaDigitalService.firmarDocumentoApp("SIMA", "123", pdfBase64, "DOCENTE");
                    pdfBase64 = objectMapper.readTree(res).get("documento").asText();
                }
                
                // Finalmente aplicar la firma actual
                String p12Base64 = Base64.getEncoder().encodeToString(archivoFirma.getBytes());
                String resFinal = firmaDigitalService.firmarDocumentoApp(p12Base64, password, pdfBase64, rolFirmante);
                String pdfFirmadoBase64 = objectMapper.readTree(resFinal).get("documento").asText();
                
                byte[] pdFirmadoBytes = Base64.getDecoder().decode(pdfFirmadoBase64);
                Files.write(filePath, pdFirmadoBytes);

            } else {
                // Modo Real: Leemos el archivo actual (que ya puede tener firmas reales PKCS7)
                pdfBytes = Files.readAllBytes(filePath);
                String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
                String p12Base64 = Base64.getEncoder().encodeToString(archivoFirma.getBytes());

                // FirmaEC maneja el estibado (stacking) de firmas reales correctamente
                String jsonRespuestaStr = firmaDigitalService.firmarDocumentoApp(p12Base64, password, pdfBase64, rolFirmante);
                String pdfFirmadoBase64 = objectMapper.readTree(jsonRespuestaStr).get("documento").asText();

                byte[] pdFirmadoBytes = Base64.getDecoder().decode(pdfFirmadoBase64);
                Files.write(filePath, pdFirmadoBytes);
            }

            // 6. Actualizar el estado del Acta vía Repository/SP
            // El SP de confirmarActa marcará confirmacionDeRol = verdadero y 
            // si los 3 roles confirmaron, automáticamente seteará ESTADO = CONFIRMADO
            // Note: El Request a ConfirmarActa necesita el ID del usuario, que lo obtenemos de la Security context en el controller, 
            // pero para invocar confirmarActa directo aquí:
            
            // actaRepo.confirmarActa ya lo teníamos, vamos a reutilizarlo
            // Usando ID ficticio 0 para el usuario porque el SP prioriza el rol en la actualización de booleanos
            actaRepo.confirmarActa(idActa, 0, rolFirmante);

            // Retornar la nueva versión del acta
            ActaEvaluacion actaActualizada = actaRepo.findById(idActa)
                .orElseThrow(() -> new RuntimeException("Error al cargar el acta actualizada tras la firma"));

            return mapToDTO(actaActualizada);

        } catch (Exception ex) {
            throw new RuntimeException("Fallo en la generación de firma: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActaEvaluacionResponseDTO> listarPorPostulacion(Integer idPostulacion) {
        return actaRepo.findByIdPostulacion(idPostulacion)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ActaEvaluacionResponseDTO mapToDTO(ActaEvaluacion e) {
        return ActaEvaluacionResponseDTO.builder()
                .idActa(e.getIdActa())
                .idPostulacion(e.getPostulacion().getIdPostulacion())
                .tipoActa(e.getTipoActa())
                .urlDocumento(e.getUrlDocumento())
                .fechaGeneracion(e.getFechaGeneracion())
                .confirmadoDecano(e.getConfirmadoDecano())
                .confirmadoCoordinador(e.getConfirmadoCoordinador())
                .confirmadoDocente(e.getConfirmadoDocente())
                .estado(e.getEstado())
                .build();
    }
}
