package org.uteq.sgacfinal.repository.ayudantia;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.response.SesionDTO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SesionRepository {

    private final JdbcTemplate jdbc;

    public Integer obtenerIdAyudantiaPorUsuario(Integer idUsuario) {
        return jdbc.queryForObject(
                "SELECT ayudantia.fn_obtener_id_ayudantia_por_usuario(?::integer)",
                Integer.class,
                idUsuario
        );
    }

    public List<SesionDTO> listarSesiones(
            Integer   idAyudantia,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String    estadoCodigo) {

        return jdbc.query(
                """
                SELECT *
                FROM ayudantia.fn_listar_sesiones(
                    ?::integer,
                    ?::date,
                    ?::date,
                    ?::varchar
                )
                """,
                new SesionRowMapper(),
                idAyudantia,
                fechaDesde  != null ? java.sql.Date.valueOf(fechaDesde)  : null,
                fechaHasta  != null ? java.sql.Date.valueOf(fechaHasta)  : null,
                estadoCodigo
        );
    }

    private static final class SesionRowMapper implements RowMapper<SesionDTO> {

        @Override
        public SesionDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            java.sql.Time horaInicioSql = rs.getTime("hora_inicio");
            java.sql.Time horaFinSql    = rs.getTime("hora_fin");
            java.sql.Date fechaSql   = rs.getDate("fecha");
            java.sql.Date fechaObsSql = rs.getDate("fecha_observacion");

            return SesionDTO.builder()
                    .idRegistroActividad(rs.getInt("id_registro_actividad"))
                    .fecha(fechaSql    != null ? fechaSql.toLocalDate()    : null)
                    .horaInicio(horaInicioSql != null ? horaInicioSql.toLocalTime() : null)
                    .horaFin(horaFinSql    != null ? horaFinSql.toLocalTime()    : null)
                    .horasDedicadas(rs.getBigDecimal("horas_dedicadas"))
                    .temaTratado(rs.getString("tema_tratado"))
                    .lugar(rs.getString("lugar"))
                    .descripcionActividad(rs.getString("descripcion_actividad"))
                    .observacionDocente(rs.getString("observaciones"))
                    .fechaObservacion(fechaObsSql != null ? fechaObsSql.toLocalDate() : null)
                    .codigoEstado(rs.getString("codigo_estado"))
                    .nombreEstado(rs.getString("nombre_estado"))
                    .build();
        }
    }
}