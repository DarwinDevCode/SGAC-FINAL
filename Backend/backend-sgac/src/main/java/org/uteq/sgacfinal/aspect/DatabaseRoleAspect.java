package org.uteq.sgacfinal.aspect;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.uteq.sgacfinal.config.UserContext;
import java.sql.Statement;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseRoleAspect {

    private final EntityManager entityManager;

    @Around("execution(* org.uteq.sgacfinal.repository.*.*(..))")
    public Object applyDatabaseRole(ProceedingJoinPoint joinPoint) throws Throwable {
        Integer userId = UserContext.getUserId();
        String appRole = UserContext.getAppRole();
        String userIp = UserContext.getUserIp();

        if (appRole != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                session.doWork(connection -> {
                    try (Statement stmt = connection.createStatement()) {
                        // 1. Rol de base de datos
                        String dbRole = "role_" + appRole.toLowerCase().replace("role_", "");
                        stmt.execute("SET ROLE " + dbRole);

                        String safeId = (userId != null) ? userId.toString() : "0";
                        String safeIp = (userIp != null && !userIp.isEmpty()) ? userIp : "127.0.0.1";

                        stmt.execute("SELECT set_config('sgac.usuario_id', '" + safeId + "', false)");
                        stmt.execute("SELECT set_config('sgac.ip_origen', '" + safeIp + "', false)");

                        log.debug(">>>> ASPECT: Contexto inyectado -> ID: {}, IP: {}", safeId, safeIp);
                    }
                });
            } catch (Exception e) {
                // Logueamos pero permitimos que continúe para no bloquear el login
                log.warn(">>>> ASPECT WARN: No se pudo inyectar contexto (posible fase de auth): {}", e.getMessage());
            }
        }

        // IMPORTANTE: NO usamos bloque 'finally' con RESET ALL aquí.
        // El RESET lo hace el pool de conexiones o se sobreescribe en la siguiente petición.
        return joinPoint.proceed();
    }
}