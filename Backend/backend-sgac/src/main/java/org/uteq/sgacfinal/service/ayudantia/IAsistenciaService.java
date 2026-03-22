package org.uteq.sgacfinal.service.ayudantia;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IAsistenciaService {
    JsonNode consultarParticipantes(Integer idAyudantia);
    JsonNode cargarParticipantesMasivo(Integer idAyudantia,
                                       List<Map<String, String>> participantes);
    byte[] generarPlantillaExcel();
    JsonNode previewExcelImport(MultipartFile file);
    JsonNode inicializarAsistencia(Integer idRegistro);
    JsonNode guardarAsistencias(Integer idRegistro,
                                List<Map<String, Object>> asistencias);
    JsonNode consultarAsistencia(Integer idRegistro);
}