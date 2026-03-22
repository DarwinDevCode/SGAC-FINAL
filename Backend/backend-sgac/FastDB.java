import java.sql.*;
public class FastDB {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        try (Connection c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SGAC-FINAL", "postgres", "Postgresql123.")) {
            String q1 = "SELECT id_usuario FROM seguridad.usuario WHERE correo LIKE 'ediazm%' OR username = 'ediazm' LIMIT 1";
            int idUsuario = -1;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q1)) {
                if (rs.next()) idUsuario = rs.getInt(1);
            }
            System.out.println("User ID: " + idUsuario);
            if (idUsuario == -1) return;
            String q2 = "SELECT a.id_ayudantia, pa.nombre_periodo, pa.estado, pa.activo as pac, c.activo as cac, p.activo as pact FROM ayudantia.ayudantia a JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria JOIN academico.periodo_academico pa ON pa.id_periodo_academico = c.id_periodo_academico JOIN academico.docente d ON d.id_docente = c.id_docente WHERE d.id_usuario = " + idUsuario;
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(q2)) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("AYUDANTIA: " + rs.getInt(1) + " | PA=" + rs.getString(2) + " | ESTADO=" + rs.getString(3) + " | PA_ACT=" + rs.getBoolean(4) + " | C_ACT=" + rs.getBoolean(5) + " | P_ACT=" + rs.getBoolean(6));
                }
                System.out.println("TOTAL COUNT: " + count);
            }
        } catch(Exception e) { e.printStackTrace(); }
    }
}
