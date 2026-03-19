package org.uteq.sgacfinal.service.impl.ayudantia;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.dto.request.ayudantia.CargarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.FinalizarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.BorradorSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.EvidenciaEliminadaResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.EvidenciaIdResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.FinalizarSesionResponseDTO;
import org.uteq.sgacfinal.entity.RegistroActividad;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.ayudantia.CierreSesionRepository;
import org.uteq.sgacfinal.repository.ayudantia.RegistroActividadRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.ayudantia.CierreSesionService;
import org.uteq.sgacfinal.service.cloudinary.CloudinaryUploadServiceImpl;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CierreSesionServiceImpl implements CierreSesionService {

    private final CierreSesionRepository cierreRepository;
    private final RegistroActividadRepository registroRepo;
    private final CloudinaryUploadServiceImpl cloudinary;
    private final INotificacionService notificacionService;

    @Override
    public RespuestaOperacionDTO<BorradorSesionResponseDTO> obtenerBorrador(Integer idUsuario, Integer idRegistro) {
        return cierreRepository.obtenerBorrador(idUsuario, idRegistro);
    }

    @Override
    public RespuestaOperacionDTO<Void> guardarProgreso(Integer idUsuario, Integer idRegistro, String descripcion) {
        return cierreRepository.guardarProgreso(idUsuario, idRegistro, descripcion);
    }

    @Override
    @Transactional
    public RespuestaOperacionDTO<EvidenciaIdResponseDTO> cargarEvidencia(
            Integer idUsuario, Integer idRegistro, Integer idTipoEvidencia, MultipartFile archivo) {

        Map<String, Object> uploadResult = cloudinary.upload(archivo);
        CargarEvidenciaRequestDTO requestBD = new CargarEvidenciaRequestDTO(
                idUsuario,
                idRegistro,
                archivo.getOriginalFilename(),
                uploadResult.get("url").toString(),
                uploadResult.get("mimeType").toString(),
                Integer.parseInt(uploadResult.get("pesoBytes").toString()),
                idTipoEvidencia
        );

        return cierreRepository.cargarEvidencia(requestBD);
    }

    @Override
    public RespuestaOperacionDTO<Void> eliminarEvidencia(Integer idUsuario, Integer idEvidencia) {
        RespuestaOperacionDTO<EvidenciaEliminadaResponseDTO> dbRes = cierreRepository.eliminarEvidencia(idUsuario, idEvidencia);

        if (dbRes.valido() && dbRes.datos() != null) {
            String url = dbRes.datos().rutaArchivo();
            String publicId = extraerPublicIdDeUrl(url);

            try {
                if (publicId != null) {
                    cloudinary.delete(publicId);
                    log.info("Evidencia eliminada físicamente de Cloudinary: {}", publicId);
                }
            } catch (Exception e) {
                log.error("Error al eliminar archivo físico de Cloudinary para la URL: {}", url, e);
            }
        }

        return new RespuestaOperacionDTO<>(dbRes.valido(), dbRes.mensaje(), null);
    }

    private String extraerPublicIdDeUrl(String url) {
        try {
            String folderPath = "sgac/documentos_academicos/";
            if (!url.contains(folderPath)) return null;

            int startIndex = url.indexOf(folderPath);
            int endIndex = url.lastIndexOf(".");

            return (endIndex != -1) ? url.substring(startIndex, endIndex) : url.substring(startIndex);
        } catch (Exception e) {
            log.error("No se pudo extraer el publicId de: {}", url);
            return null;
        }
    }

    @Override
    @Transactional
    public RespuestaOperacionDTO<FinalizarSesionResponseDTO> finalizarSesion(FinalizarSesionRequestDTO request) {
        RespuestaOperacionDTO<FinalizarSesionResponseDTO> response = cierreRepository.finalizarSesion(request);
        if (response.valido() && response.datos() != null) {
            notificarCierreDocente(request.idRegistro());
        }

        return response;
    }

    private void notificarCierreDocente(Integer idRegistro) {
        try {
            RegistroActividad actividad = registroRepo.findById(idRegistro).orElseThrow();
            Usuario docente = actividad.getAyudantia().getPostulacion().getConvocatoria().getDocente().getUsuario();
            Usuario ayudante = actividad.getAyudantia().getPostulacion().getEstudiante().getUsuario();

            NotificationRequest wsReq = new NotificationRequest();
            wsReq.setTitulo("Sesión pendiente de revisión");
            wsReq.setMensaje(String.format("El ayudante %s ha finalizado la sesión del %s. Está lista para su evaluación.",
                    ayudante.getNombres(), actividad.getFecha()));
            wsReq.setTipo("ALERTA");
            wsReq.setIdReferencia(idRegistro);

            notificacionService.enviarNotificacion(docente.getIdUsuario(), wsReq);

        } catch (Exception ex) {
            log.error("[CierreSesionService] Error al notificar el cierre de la sesión {}: {}", idRegistro, ex.getMessage());
        }
    }
}