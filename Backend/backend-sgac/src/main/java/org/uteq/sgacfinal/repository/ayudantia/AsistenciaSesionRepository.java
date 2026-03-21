package org.uteq.sgacfinal.repository.ayudantia;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.request.ayudantia.PlanificarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.AsistenciaSesionActualResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.MarcadoAsistenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.PlanificacionResponseDTO;
import org.uteq.sgacfinal.util.DatabaseService;

@Repository
@RequiredArgsConstructor
public class AsistenciaSesionRepository {
    private final DatabaseService db;

    public RespuestaOperacionDTO<PlanificacionResponseDTO> planificarSesion(PlanificarSesionRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", req.idUsuario())
                .addValue("p_fecha", req.fecha())
                .addValue("p_hora_inicio", req.horaInicio())
                .addValue("p_hora_fin", req.horaFin())
                .addValue("p_lugar", req.lugar())
                .addValue("p_tema", req.tema());

        return db.ejecutarFuncion("ayudantia", "fn_planificar_sesion", params,
                new TypeReference<PlanificacionResponseDTO>() {});
    }

    public RespuestaOperacionDTO<AsistenciaSesionActualResponseDTO> obtenerAsistenciaSesionActual(Integer idUsuario) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario);

        return db.ejecutarFuncion("ayudantia", "fn_obtener_asistencia_sesion_actual", params,
                new TypeReference<AsistenciaSesionActualResponseDTO>() {});
    }

    public RespuestaOperacionDTO<Void> marcarAsistencia(MarcadoAsistenciaRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_detalle", req.idDetalle())
                .addValue("p_asistio", req.asistio());

        return db.ejecutarFuncion("ayudantia", "fn_marcar_asistencia", params,
                new TypeReference<Void>() {});
    }

    public RespuestaOperacionDTO<AsistenciaSesionActualResponseDTO> obtenerAsistenciaPorId(Integer idUsuario, Integer idRegistro) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario)
                .addValue("p_id_registro", idRegistro);

        return db.ejecutarFuncion("ayudantia", "fn_obtener_asistencia_por_id", params,
                new TypeReference<AsistenciaSesionActualResponseDTO>() {});
    }
}