package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.MetricasConvocatoriaDTO;
import org.uteq.sgacfinal.entity.Notificacion;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.NotificacionRepository;

import java.util.List;
import java.util.Map;

/**
 * P10 (Ítems 11/12/13): Servicio para notificaciones masivas por rol o a todos.
 * P12 (Ítem 3): Provee KPIs del dashboard.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionMasivaService {

    private final NotificacionRepository notificacionRepository;
    private final IUsuariosRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Envía una notificación masiva usando el SP en BD.
     * @param mensaje           Texto de la notificación
     * @param tipo              Tipo de mensaje (CONVOCATORIA, GENERAL, etc.)
     * @param tipoNotificacion  MASIVA_ROL | MASIVA_TODOS
     * @param idRol             null = todos, o el id_tipo_rol
     * @param idConvocatoria    null o id de la convocatoria relacionada
     * @return cantidad de notificaciones enviadas
     */
    public int enviarMasiva(String mensaje, String tipo, String tipoNotificacion,
                            Integer idRol, Integer idConvocatoria) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT public.sp_enviar_notificacion_masiva(?, ?, ?, ?, ?)",
                Integer.class,
                mensaje, tipo, tipoNotificacion, idRol, idConvocatoria
        );
        return count != null ? count : 0;
    }

    /**
     * Envía una notificación individual directamente (sin SP masivo).
     */
    public void enviarIndividual(Integer idUsuario, String mensaje, String tipo) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
        Notificacion n = Notificacion.builder()
                .usuarioDestino(u)
                .mensaje(mensaje)
                .tipo(tipo)
                .tipoNotificacion("INDIVIDUAL")
                .leido(false)
                .build();
        notificacionRepository.save(n);
    }

    // ---- P12: Dashboard KPIs coordinador ----

    public MetricasConvocatoriaDTO dashboardCoordinador(Integer idCarrera) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM public.sp_metricas_convocatoria(?)",
                (rs, r) -> MetricasConvocatoriaDTO.builder()
                        .totalConvocatorias(rs.getLong("total_convocatorias"))
                        .convocatoriasActivas(rs.getLong("convocatorias_activas"))
                        .totalPostulaciones(rs.getLong("total_postulaciones"))
                        .postulacionesPendientes(rs.getLong("postulaciones_pendientes"))
                        .postulacionesAprobadas(rs.getLong("postulaciones_aprobadas"))
                        .build(),
                idCarrera
        );
    }

    public Map<String, Long> dashboardPostulante(Integer idUsuario) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM public.sp_metricas_postulante(?)",
                (rs, r) -> Map.of(
                        "totalPostulaciones",       rs.getLong("total_postulaciones"),
                        "postulacionesPendientes",  rs.getLong("postulaciones_pendientes"),
                        "postulacionesAprobadas",   rs.getLong("postulaciones_aprobadas"),
                        "postulacionesRechazadas",  rs.getLong("postulaciones_rechazadas")
                ),
                idUsuario
        );
    }
}
