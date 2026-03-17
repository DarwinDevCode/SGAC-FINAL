package org.uteq.sgacfinal.repository.convocatorias;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.Response.convocatorias.TribunalEvaluacionResponseDTO;
import org.uteq.sgacfinal.util.DatabaseService;

@Repository
@RequiredArgsConstructor
public class PostulacionTypeRepository {

    private final DatabaseService db;

    public RespuestaOperacionDTO<TribunalEvaluacionResponseDTO> obtenerTribunalEvaluacion(Integer idUsuario) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario);

        return db.ejecutarFuncion(
                "postulacion",
                "fn_obtener_tribunal_evaluacion",
                params,
                new TypeReference<TribunalEvaluacionResponseDTO>() {}
        );
    }
}