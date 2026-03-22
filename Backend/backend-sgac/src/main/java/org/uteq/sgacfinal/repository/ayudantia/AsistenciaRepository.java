package org.uteq.sgacfinal.repository.ayudantia;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AsistenciaRepository {

    private final JdbcTemplate jdbcTemplate;

    public String consultarParticipantes(Integer idAyudantia) {
        return jdbcTemplate.queryForObject(
                "SELECT ayudantia.fn_consultar_participantes(?::integer)",
                String.class,
                idAyudantia
        );
    }

    public String cargarParticipantesMasivo(Integer idAyudantia, String participantesJson) {
        return jdbcTemplate.queryForObject(
                "SELECT ayudantia.fn_cargar_participantes_masivo(?::integer, ?::jsonb)",
                String.class,
                idAyudantia,
                participantesJson
        );
    }

    public String inicializarAsistencia(Integer idRegistro) {
        return jdbcTemplate.queryForObject(
                "SELECT ayudantia.fn_inicializar_asistencia(?::integer)",
                String.class,
                idRegistro
        );
    }

    public String guardarAsistencias(Integer idRegistro, String asistenciasJson) {
        return jdbcTemplate.queryForObject(
                "SELECT ayudantia.fn_guardar_asistencias(?::integer, ?::jsonb)",
                String.class,
                idRegistro,
                asistenciasJson
        );
    }

    public String consultarAsistencia(Integer idRegistro) {
        return jdbcTemplate.queryForObject(
                "SELECT ayudantia.fn_consultar_asistencia(?::integer)",
                String.class,
                idRegistro
        );
    }
}