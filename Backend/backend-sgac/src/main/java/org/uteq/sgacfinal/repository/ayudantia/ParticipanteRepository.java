package org.uteq.sgacfinal.repository.ayudantia;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.response.ayudantia.ParticipanteIdResponseDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.ParticipanteRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.util.DatabaseService;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParticipanteRepository {
    private final DatabaseService db;

    public RespuestaOperacionDTO<ParticipanteIdResponseDTO> gestionarParticipante(ParticipanteRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_accion", req.accion())
                .addValue("p_id_usuario", req.idUsuario())
                .addValue("p_nombre", req.nombre())
                .addValue("p_curso", req.curso())
                .addValue("p_paralelo", req.paralelo())
                .addValue("p_id_participante", req.idParticipante());

        return db.ejecutarFuncion("ayudantia", "fn_gestionar_participante", params,
                new TypeReference<ParticipanteIdResponseDTO>() {});
    }
}