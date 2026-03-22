package org.uteq.sgacfinal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestDBDebug {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testMisAyudantes() {
        try {
            String q1 = "SELECT id_usuario, nombres, apellidos FROM seguridad.usuario " +
                        "WHERE correo LIKE 'ediazm%' OR username = 'ediazm' LIMIT 1";
            Map<String, Object> user = jdbcTemplate.queryForMap(q1);
            Integer idUsuario = (Integer) user.get("id_usuario");
            System.out.println("========== DB DEBUG ==========");
            System.out.println("User: " + user.get("nombres") + " " + user.get("apellidos") + " ID: " + idUsuario);

            String q2 = "SELECT a.id_ayudantia, pa.nombre_periodo, pa.estado as pa_estado, pa.activo as pa_activo, " +
                        "c.activo as c_activo, p.activo as p_activo " +
                        "FROM ayudantia.ayudantia a " +
                        "JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion " +
                        "JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria " +
                        "JOIN academico.periodo_academico pa ON pa.id_periodo_academico = c.id_periodo_academico " +
                        "JOIN academico.docente d ON d.id_docente = c.id_docente " +
                        "WHERE d.id_usuario = " + idUsuario;
            List<Map<String, Object>> res = jdbcTemplate.queryForList(q2);
            System.out.println("Found " + res.size() + " ayudantias para d.id_usuario = " + idUsuario);
            for (Map<String, Object> row : res) {
                System.out.println(row);
            }
            System.out.println("==============================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
