package org.uteq.sgacfinal.service.ayudantia;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IAsistenciaService {
    Integer resolverIdAyudantia();
    Integer resolverIdRegistro();
    Object consultarParticipantes(Integer idAyudantia);
    Object cargarParticipantesMasivo(Integer idAyudantia,
                                     List<Map<String, String>> participantes);
    byte[] generarPlantillaExcel();
    Object previewExcelImport(MultipartFile file);
    Object inicializarAsistencia(Integer idRegistro);
    Object guardarAsistencias(Integer idRegistro,
                              List<Map<String, Object>> asistencias);
    Object consultarAsistencia(Integer idRegistro);

    Object obtenerMatrizAsistencia();
}