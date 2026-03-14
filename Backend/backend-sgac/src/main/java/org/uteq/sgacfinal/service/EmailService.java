package org.uteq.sgacfinal.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    private static final String SYSTEM_NAME = "SGAC — Sistema de Gestión de Ayudantías de Cátedra";
    private static final String SYSTEM_URL  = "http://localhost:5173"; // ajustar en producción

    @Async
    public void enviarCredenciales(
            String destinatario,
            String nombreCompleto,
            String username,
            String passwordTemporal,
            List<String> roles) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, SYSTEM_NAME);
            helper.setTo(destinatario);
            helper.setSubject("🎓 Bienvenido al SGAC — Sus credenciales de acceso");
            helper.setText(buildHtmlTemplate(nombreCompleto, username, passwordTemporal, roles), true);

            mailSender.send(message);
            log.info("[EmailService] Credenciales enviadas a: {}", destinatario);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[EmailService] Error al enviar correo a {}: {}", destinatario, e.getMessage());
        }
    }

    private String buildHtmlTemplate(
            String nombreCompleto,
            String username,
            String password,
            List<String> roles) {

        String rolesHtml = roles.stream()
                .map(r -> "<li style='padding:4px 0;'>✅ " + formatRolName(r) + "</li>")
                .reduce("", String::concat);

        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <title>Credenciales SGAC</title>
        </head>
        <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Arial,sans-serif;">

          <!-- Wrapper -->
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f4f8;padding:32px 16px;">
            <tr><td align="center">

              <!-- Card -->
              <table width="600" cellpadding="0" cellspacing="0"
                     style="background:#ffffff;border-radius:12px;
                            box-shadow:0 4px 24px rgba(0,0,0,.08);overflow:hidden;max-width:600px;">

                <!-- Header -->
                <tr>
                  <td style="background:linear-gradient(135deg,#1e3a5f 0%%,#2563eb 100%%);
                              padding:36px 40px;text-align:center;">
                    <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;letter-spacing:-.5px;">
                      🎓 SGAC
                    </h1>
                    <p style="margin:8px 0 0;color:#bfdbfe;font-size:13px;">
                      Sistema de Gestión de Ayudantías de Cátedra — UTEQ
                    </p>
                  </td>
                </tr>

                <!-- Body -->
                <tr>
                  <td style="padding:40px;">

                    <p style="margin:0 0 8px;font-size:22px;font-weight:700;color:#1e3a5f;">
                      ¡Bienvenido/a, %s!
                    </p>
                    <p style="margin:0 0 28px;font-size:15px;color:#475569;line-height:1.6;">
                      Su cuenta ha sido creada exitosamente en el SGAC.
                      A continuación encontrará sus credenciales de acceso.
                    </p>

                    <!-- Credentials box -->
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#f8fafc;border:1px solid #e2e8f0;
                                  border-radius:8px;margin-bottom:28px;">
                      <tr>
                        <td style="padding:24px;">
                          <p style="margin:0 0 16px;font-size:13px;font-weight:600;
                                     color:#64748b;text-transform:uppercase;letter-spacing:.8px;">
                            Credenciales de Acceso
                          </p>

                          <table width="100%%">
                            <tr>
                              <td width="40%%" style="padding:8px 0;font-size:14px;color:#64748b;font-weight:600;">
                                Usuario:
                              </td>
                              <td style="padding:8px 0;">
                                <span style="font-family:monospace;font-size:16px;font-weight:700;
                                             color:#1e3a5f;background:#e0f2fe;padding:4px 10px;
                                             border-radius:4px;">
                                  %s
                                </span>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:8px 0;font-size:14px;color:#64748b;font-weight:600;">
                                Contraseña temporal:
                              </td>
                              <td style="padding:8px 0;">
                                <span style="font-family:monospace;font-size:16px;font-weight:700;
                                             color:#1e3a5f;background:#fef3c7;padding:4px 10px;
                                             border-radius:4px;letter-spacing:2px;">
                                  %s
                                </span>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>

                    <!-- Roles -->
                    <p style="margin:0 0 8px;font-size:14px;font-weight:600;color:#475569;">
                      Roles asignados:
                    </p>
                    <ul style="margin:0 0 28px;padding-left:20px;color:#334155;font-size:14px;line-height:1.8;">
                      %s
                    </ul>

                    <!-- Warning -->
                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:#fff7ed;border-left:4px solid #f59e0b;
                                  border-radius:0 8px 8px 0;margin-bottom:32px;">
                      <tr>
                        <td style="padding:16px 20px;">
                          <p style="margin:0;font-size:14px;color:#92400e;font-weight:600;">
                            ⚠️ Aviso de seguridad
                          </p>
                          <p style="margin:6px 0 0;font-size:13px;color:#78350f;line-height:1.5;">
                            Esta es una contraseña temporal generada automáticamente.
                            Por su seguridad, le recomendamos cambiarla en su primer ingreso
                            al sistema desde la opción <strong>Perfil → Cambiar contraseña</strong>.
                          </p>
                        </td>
                      </tr>
                    </table>

                    <!-- CTA Button -->
                    <div style="text-align:center;margin-bottom:32px;">
                      <a href="%s"
                         style="display:inline-block;background:linear-gradient(135deg,#1e3a5f,#2563eb);
                                color:#ffffff;text-decoration:none;padding:14px 40px;
                                border-radius:8px;font-size:15px;font-weight:600;
                                letter-spacing:.3px;">
                        Acceder al Sistema →
                      </a>
                    </div>

                    <p style="margin:0;font-size:13px;color:#94a3b8;text-align:center;line-height:1.6;">
                      Si no esperaba este correo o tiene alguna duda,
                      comuníquese con el administrador del sistema.
                    </p>

                  </td>
                </tr>

                <!-- Footer -->
                <tr>
                  <td style="background:#f8fafc;padding:20px 40px;border-top:1px solid #e2e8f0;">
                    <p style="margin:0;font-size:12px;color:#94a3b8;text-align:center;">
                      © 2025 Universidad Técnica Estatal de Quevedo — SGAC<br/>
                      Este es un correo automático, por favor no responda a este mensaje.
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>

        </body>
        </html>
        """.formatted(nombreCompleto, username, password, rolesHtml, SYSTEM_URL);
    }



    @Async
    public void enviarActualizacionCarga(
            String destinatario,
            String nombreDocente,
            List<String> asignaturasActuales,
            List<String> asignaturasRevocadas) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("equipoti28@gmail.com", SYSTEM_NAME);
            helper.setTo(destinatario);
            helper.setSubject("📚 SGAC — Actualización de su carga académica");
            helper.setText(buildCargaTemplate(nombreDocente, asignaturasActuales, asignaturasRevocadas), true);

            mailSender.send(message);
            log.info("[EmailService] Notificación de carga enviada a: {}", destinatario);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("[EmailService] Error al enviar notificación de carga a {}: {}", destinatario, e.getMessage());
        }
    }

    private String buildCargaTemplate(
            String nombreDocente,
            List<String> actuales,
            List<String> revocadas) {

        String filaActuales = (actuales == null || actuales.isEmpty())
                ? "<tr><td style='padding:12px;color:#64748b;font-style:italic;'>Sin asignaturas asignadas.</td></tr>"
                : actuales.stream()
                .map(a -> "<tr><td style='padding:8px 12px;border-bottom:1px solid #f1f5f9;'>"
                        + "📖 " + a + "</td></tr>")
                .reduce("", String::concat);

        String bloqueRevocadas = "";
        if (revocadas != null && !revocadas.isEmpty()) {
            String filas = revocadas.stream()
                    .map(r -> "<tr><td style='padding:8px 12px;border-bottom:1px solid #fff1f2;'>"
                            + "❌ " + r + "</td></tr>")
                    .reduce("", String::concat);

            bloqueRevocadas = """
                <p style="margin:28px 0 8px;font-size:14px;font-weight:600;color:#991b1b;">
                  Asignaturas revocadas en esta actualización:
                </p>
                <table width="100%%" style="background:#fff1f2;border:1px solid #fecaca;
                       border-radius:8px;border-collapse:collapse;margin-bottom:24px;">
                  %s
                </table>
                """.formatted(filas);
        }

        return """
        <!DOCTYPE html>
        <html lang="es">
        <head><meta charset="UTF-8"/></head>
        <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f4f8;padding:32px 16px;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0"
                     style="background:#fff;border-radius:12px;
                            box-shadow:0 4px 24px rgba(0,0,0,.08);overflow:hidden;max-width:600px;">

                <!-- Header -->
                <tr>
                  <td style="background:linear-gradient(135deg,#1e3a5f,#2563eb);padding:32px 40px;text-align:center;">
                    <h1 style="margin:0;color:#fff;font-size:22px;font-weight:700;">📚 SGAC</h1>
                    <p style="margin:8px 0 0;color:#bfdbfe;font-size:13px;">
                      Actualización de Carga Académica
                    </p>
                  </td>
                </tr>

                <!-- Body -->
                <tr><td style="padding:36px 40px;">

                  <p style="margin:0 0 6px;font-size:20px;font-weight:700;color:#1e3a5f;">
                    Estimado/a %s,
                  </p>
                  <p style="margin:0 0 28px;font-size:15px;color:#475569;line-height:1.6;">
                    Su carga académica ha sido actualizada por el administrador del sistema.
                    A continuación encontrará el resumen de los cambios.
                  </p>

                  <!-- Asignaturas actuales -->
                  <p style="margin:0 0 8px;font-size:14px;font-weight:600;color:#166534;">
                    ✅ Carga académica actual:
                  </p>
                  <table width="100%%" style="background:#f0fdf4;border:1px solid #bbf7d0;
                         border-radius:8px;border-collapse:collapse;margin-bottom:24px;">
                    %s
                  </table>

                  %s

                  <!-- Aviso -->
                  <table width="100%%" style="background:#fffbeb;border-left:4px solid #f59e0b;
                         border-radius:0 8px 8px 0;">
                    <tr><td style="padding:14px 18px;font-size:13px;color:#78350f;">
                      Si tiene alguna duda sobre estos cambios, comuníquese con la coordinación académica.
                    </td></tr>
                  </table>

                </td></tr>

                <!-- Footer -->
                <tr>
                  <td style="background:#f8fafc;padding:18px 40px;border-top:1px solid #e2e8f0;">
                    <p style="margin:0;font-size:12px;color:#94a3b8;text-align:center;">
                      © 2025 SGAC — UTEQ · Correo automático, no responder.
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(nombreDocente, filaActuales, bloqueRevocadas);
    }

    private String formatRolName(String rol) {
        return switch (rol.toUpperCase()) {
            case "ESTUDIANTE"      -> "Estudiante";
            case "DOCENTE"         -> "Docente";
            case "COORDINADOR"     -> "Coordinador/a";
            case "DECANO"          -> "Decano/a";
            case "ADMINISTRADOR"   -> "Administrador/a";
            case "AYUDANTE_CATEDRA"-> "Ayudante de Cátedra";
            default                -> rol;
        };
    }
}