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
    private static final String SYSTEM_URL  = "http://localhost:5173";

    private static final String GREEN_PRIMARY = "#1B5E20";
    private static final String GREEN_LIGHT   = "#f0fdf4";
    private static final String GREEN_GRADIENT = "linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%)";

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
            helper.setSubject("🎓 Bienvenido al SGAC — Credenciales de acceso");
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
                .map(r -> "<li style='padding:4px 0;'>✔️ " + formatRolName(r) + "</li>")
                .reduce("", String::concat);

        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>
        <body style="margin:0;padding:0;background:#f9fafb;font-family:'Segoe UI',Arial,sans-serif;">

          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f9fafb;padding:32px 16px;">
            <tr><td align="center">

              <table width="600" cellpadding="0" cellspacing="0"
                     style="background:#ffffff;border-radius:12px;
                            box-shadow:0 10px 15px -3px rgba(0,0,0,0.1);overflow:hidden;max-width:600px;border:1px solid #e5e7eb;">

                <tr>
                  <td style="background:%s; padding:40px;text-align:center;">
                    <h1 style="margin:0;color:#ffffff;font-size:28px;font-weight:800;letter-spacing:-1px;">
                      SGAC
                    </h1>
                    <p style="margin:8px 0 0;color:#dcfce7;font-size:14px;font-weight:500;">
                      Universidad Técnica Estatal de Quevedo
                    </p>
                  </td>
                </tr>

                <tr>
                  <td style="padding:40px;">

                    <p style="margin:0 0 8px;font-size:22px;font-weight:700;color:%s;">
                      ¡Bienvenido/a al Sistema!
                    </p>
                    <p style="margin:0 0 28px;font-size:15px;color:#4b5563;line-height:1.6;">
                      Hola <strong>%s</strong>, su cuenta ha sido habilitada en el Sistema de Gestión de Ayudantías. 
                      Use los siguientes datos para ingresar:
                    </p>

                    <table width="100%%" cellpadding="0" cellspacing="0"
                           style="background:%s;border:1px solid #bbf7d0;
                                  border-radius:12px;margin-bottom:28px;">
                      <tr>
                        <td style="padding:24px;">
                          <table width="100%%">
                            <tr>
                              <td width="40%%" style="padding:8px 0;font-size:14px;color:#166534;font-weight:600;">
                                Usuario:
                              </td>
                              <td style="padding:8px 0;">
                                <span style="font-family:monospace;font-size:16px;font-weight:700;
                                             color:%s;background:#ffffff;padding:6px 12px;
                                             border:1px solid #bbf7d0;border-radius:6px;">
                                  %s
                                </span>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:8px 0;font-size:14px;color:#166534;font-weight:600;">
                                Contraseña temporal:
                              </td>
                              <td style="padding:8px 0;">
                                <span style="font-family:monospace;font-size:16px;font-weight:700;
                                             color:%s;background:#ffffff;padding:6px 12px;
                                             border:1px solid #bbf7d0;border-radius:6px;">
                                  %s
                                </span>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>

                    <p style="margin:0 0 8px;font-size:14px;font-weight:700;color:#374151;">
                      Roles asignados:
                    </p>
                    <ul style="margin:0 0 28px;padding-left:20px;color:#4b5563;font-size:14px;line-height:1.8;">
                      %s
                    </ul>

                    <div style="text-align:center;margin-bottom:32px;">
                      <a href="%s"
                         style="display:inline-block;background:%s;
                                color:#ffffff;text-decoration:none;padding:16px 48px;
                                border-radius:10px;font-size:15px;font-weight:700;
                                box-shadow:0 4px 6px -1px rgba(0,0,0,0.1);">
                        Iniciar Sesión Ahora
                      </a>
                    </div>

                    <p style="margin:0;font-size:12px;color:#9ca3af;text-align:center;line-height:1.5;">
                      Por seguridad, cambie su contraseña después del primer ingreso.<br/>
                      Si no reconoce esta solicitud, ignore este mensaje.
                    </p>

                  </td>
                </tr>

                <tr>
                  <td style="background:#f9fafb;padding:24px;border-top:1px solid #f3f4f6;text-align:center;">
                    <p style="margin:0;font-size:11px;color:#9ca3af;text-transform:uppercase;letter-spacing:1px;">
                      © 2025 UTEQ — UNIDAD DE TECNOLOGÍAS DE INFORMACIÓN
                    </p>
                  </td>
                </tr>

              </table>
            </td></tr>
          </table>

        </body>
        </html>
        """.formatted(GREEN_GRADIENT, GREEN_PRIMARY, nombreCompleto, GREEN_LIGHT, GREEN_PRIMARY, username, GREEN_PRIMARY, password, rolesHtml, SYSTEM_URL, GREEN_PRIMARY);
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

            helper.setFrom(fromAddress, SYSTEM_NAME);
            helper.setTo(destinatario);
            helper.setSubject("📚 SGAC — Actualización de Carga Académica");
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
                ? "<tr><td style='padding:16px;color:#9ca3af;font-style:italic;'>No hay asignaturas registradas.</td></tr>"
                : actuales.stream()
                .map(a -> "<tr><td style='padding:12px 16px;border-bottom:1px solid #e5e7eb;color:#374151;font-size:14px;'>"
                        + "<span style='color:" + GREEN_PRIMARY + ";margin-right:10px;'>●</span>" + a + "</td></tr>")
                .reduce("", String::concat);

        String bloqueRevocadas = "";
        if (revocadas != null && !revocadas.isEmpty()) {
            String filas = revocadas.stream()
                    .map(r -> "<tr><td style='padding:10px 16px;border-bottom:1px solid #fee2e2;color:#991b1b;font-size:13px;'>"
                            + "✕ " + r + "</td></tr>")
                    .reduce("", String::concat);

            bloqueRevocadas = """
                <p style="margin:24px 0 10px;font-size:14px;font-weight:700;color:#b91c1c;">
                  Asignaturas retiradas:
                </p>
                <table width="100%%" style="background:#fef2f2;border:1px solid #fecaca;
                       border-radius:10px;border-collapse:collapse;margin-bottom:20px;">
                  %s
                </table>
                """.formatted(filas);
        }

        return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"/></head>
        <body style="margin:0;padding:0;background:#f9fafb;font-family:'Segoe UI',Arial,sans-serif;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f9fafb;padding:32px 16px;">
            <tr><td align="center">
              <table width="600" cellpadding="0" cellspacing="0"
                     style="background:#fff;border-radius:12px;box-shadow:0 10px 15px -3px rgba(0,0,0,0.1);max-width:600px;border:1px solid #e5e7eb;">

                <tr>
                  <td style="background:%s;padding:32px 40px;text-align:center;">
                    <h1 style="margin:0;color:#fff;font-size:24px;font-weight:800;">SGAC</h1>
                    <p style="margin:4px 0 0;color:#dcfce7;font-size:13px;font-weight:500;">Gestión Académica</p>
                  </td>
                </tr>

                <tr><td style="padding:40px;">
                  <p style="margin:0 0 6px;font-size:20px;font-weight:700;color:%s;">
                    Estimado/a %s,
                  </p>
                  <p style="margin:0 0 24px;font-size:15px;color:#4b5563;line-height:1.6;">
                    Se ha realizado una actualización en su carga académica en el sistema SGAC.
                  </p>

                  <p style="margin:0 0 10px;font-size:14px;font-weight:700;color:%s;">
                    Asignaturas vigentes:
                  </p>
                  <table width="100%%" style="background:%s;border:1px solid #bbf7d0;
                         border-radius:10px;border-collapse:collapse;margin-bottom:20px;">
                    %s
                  </table>

                  %s

                  <div style="margin-top:32px;padding:20px;background:#fffbeb;border-left:4px solid #f59e0b;border-radius:4px;">
                    <p style="margin:0;font-size:13px;color:#92400e;line-height:1.5;">
                      <strong>Nota:</strong> Si los cambios no coinciden con su distributivo oficial, por favor contacte a su Coordinador de Carrera.
                    </p>
                  </div>

                </td></tr>

                <tr>
                  <td style="background:#f9fafb;padding:20px;border-top:1px solid #f3f4f6;">
                    <p style="margin:0;font-size:11px;color:#9ca3af;text-align:center;">
                      © 2025 UTEQ — SGAC · Sistema Automático de Notificaciones
                    </p>
                  </td>
                </tr>
              </table>
            </td></tr>
          </table>
        </body>
        </html>
        """.formatted(GREEN_GRADIENT, GREEN_PRIMARY, nombreDocente, GREEN_PRIMARY, GREEN_LIGHT, filaActuales, bloqueRevocadas);
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