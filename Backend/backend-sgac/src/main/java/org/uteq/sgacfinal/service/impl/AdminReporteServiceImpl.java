package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.AdminReporteGlobalDTO;
import org.uteq.sgacfinal.dto.response.AuditoriaGlobalDTO;
import org.uteq.sgacfinal.service.IAdminReporteService;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReporteServiceImpl implements IAdminReporteService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AdminReporteGlobalDTO.UsuarioDTO> reporteGlobalUsuarios() {
        String sql = "SELECT * FROM seguridad.fn_reporte_global_usuarios()";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            AdminReporteGlobalDTO.UsuarioDTO.builder()
                .usuario(rs.getString("usuario"))
                .email(rs.getString("email"))
                .roles(rs.getString("roles"))
                .estado(rs.getString("estado"))
                .build()
        );
    }

    @Override
    public List<AdminReporteGlobalDTO.PersonalDTO> reporteGlobalPersonal() {
        String sql = "SELECT * FROM academico.fn_reporte_global_personal()";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            AdminReporteGlobalDTO.PersonalDTO.builder()
                .nombre(rs.getString("nombre"))
                .cargoContexto(rs.getString("cargo_contexto"))
                .estado(rs.getString("estado"))
                .build()
        );
    }

    @Override
    public List<AdminReporteGlobalDTO.PostulanteDTO> reporteGlobalPostulantes() {
        String sql = "SELECT * FROM postulacion.fn_reporte_global_postulantes()";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            AdminReporteGlobalDTO.PostulanteDTO.builder()
                .estudiante(rs.getString("estudiante"))
                .cedula(rs.getString("cedula"))
                .asignatura(rs.getString("asignatura"))
                .periodo(rs.getString("periodo"))
                .estado(rs.getString("estado"))
                .build()
        );
    }

    @Override
    public List<AdminReporteGlobalDTO.AyudanteDTO> reporteGlobalAyudantes() {
        String sql = "SELECT * FROM ayudantia.fn_reporte_global_ayudantes()";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            AdminReporteGlobalDTO.AyudanteDTO.builder()
                .estudiante(rs.getString("estudiante"))
                .asignatura(rs.getString("asignatura"))
                .docente(rs.getString("docente"))
                .horas(rs.getDouble("horas"))
                .estado(rs.getString("estado"))
                .build()
        );
    }

    @Override
    public List<AuditoriaGlobalDTO> reporteAuditoriaGlobal() {
        String sql = "SELECT * FROM notificacion.fn_auditoria_global()";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp ts = rs.getTimestamp("fecha_hora");
            return AuditoriaGlobalDTO.builder()
                .idLog(rs.getInt("id_log_auditoria"))
                .fecha(ts != null ? ts.toLocalDateTime() : null)
                .usuario(rs.getString("usuario"))
                .roles(rs.getString("roles"))
                .facultad(rs.getString("facultad"))
                .carrera(rs.getString("carrera"))
                .accion(rs.getString("accion"))
                .modulo(rs.getString("tabla_afectada"))
                .detalle(rs.getString("detalle"))
                .build();
        });
    }
}
