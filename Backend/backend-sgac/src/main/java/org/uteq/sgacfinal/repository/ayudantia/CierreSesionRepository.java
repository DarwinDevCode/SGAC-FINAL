package org.uteq.sgacfinal.repository.ayudantia;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.request.ayudantia.CargarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.FinalizarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.BorradorSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.EvidenciaEliminadaResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.EvidenciaIdResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.FinalizarSesionResponseDTO;
import org.uteq.sgacfinal.util.DatabaseService;

@Repository
@RequiredArgsConstructor
public class CierreSesionRepository {
    private final DatabaseService db;

    public RespuestaOperacionDTO<BorradorSesionResponseDTO> obtenerBorrador(Integer idUsuario, Integer idRegistro) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario)
                .addValue("p_id_registro", idRegistro);

        return db.ejecutarFuncion("ayudantia", "fn_obtener_borrador_sesion", params,
                new TypeReference<BorradorSesionResponseDTO>() {});
    }

    public RespuestaOperacionDTO<Void> guardarProgreso(Integer idUsuario, Integer idRegistro, String descripcion) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario)
                .addValue("p_id_registro", idRegistro)
                .addValue("p_descripcion", descripcion);

        return db.ejecutarFuncion("ayudantia", "fn_guardar_progreso_sesion", params,
                new TypeReference<Void>() {});
    }

    public RespuestaOperacionDTO<EvidenciaIdResponseDTO> cargarEvidencia(CargarEvidenciaRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", req.idUsuario())
                .addValue("p_id_registro", req.idRegistro())
                .addValue("p_nombre_archivo", req.nombreArchivo())
                .addValue("p_ruta_archivo", req.rutaArchivo())
                .addValue("p_mime_type", req.mimeType())
                .addValue("p_tamanio_bytes", req.tamanioBytes())
                .addValue("p_id_tipo_evidencia", req.idTipoEvidencia());

        return db.ejecutarFuncion("ayudantia", "fn_cargar_evidencia_sesion", params,
                new TypeReference<EvidenciaIdResponseDTO>() {});
    }


    public RespuestaOperacionDTO<EvidenciaEliminadaResponseDTO> eliminarEvidencia(Integer idUsuario, Integer idEvidencia) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario)
                .addValue("p_id_evidencia", idEvidencia);

        return db.ejecutarFuncion("ayudantia", "fn_eliminar_evidencia_sesion", params,
                new TypeReference<EvidenciaEliminadaResponseDTO>() {});
    }

    public RespuestaOperacionDTO<FinalizarSesionResponseDTO> finalizarSesion(FinalizarSesionRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", req.idUsuario())
                .addValue("p_id_registro", req.idRegistro())
                .addValue("p_descripcion", req.descripcion());

        return db.ejecutarFuncion("ayudantia", "fn_finalizar_sesion", params,
                new TypeReference<FinalizarSesionResponseDTO>() {});
    }
}