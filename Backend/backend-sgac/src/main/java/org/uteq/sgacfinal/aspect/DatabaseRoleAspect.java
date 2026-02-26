package org.uteq.sgacfinal.aspect;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.uteq.sgacfinal.config.UserContext;

import java.sql.Statement;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseRoleAspect {
    private final EntityManager entityManager;

    @Around("execution(* org.uteq.sgacfinal.repository.*.*(..)) " +
            "&& !execution(* org.uteq.sgacfinal.repository.NotificacionRepository.*(..)) " +
            "&& !execution(* org.uteq.sgacfinal.repository.IUsuariosRepository.*(..))")
    public Object applyDatabaseRole(ProceedingJoinPoint joinPoint) throws Throwable {
        String appRole = UserContext.getAppRole();
        boolean roleChanged = false;


        if (appRole != null && !appRole.isEmpty()) {
            String dbRole = appRole.toLowerCase().startsWith("role_")
                    ? appRole.toLowerCase()
                    : "role_" + appRole.toLowerCase();

            try {
                Session session = entityManager.unwrap(Session.class);
                session.doWork(connection -> {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("SET LOCAL ROLE " + dbRole);
                        log.debug("Rol de BD cambiado exitosamente a: {}", dbRole);
                    }
                });
                roleChanged = true;
            } catch (Exception e) {
                log.error("No se pudo hacer SET ROLE en la base de datos", e);
            }
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (roleChanged) {
                try {
                    Session session = entityManager.unwrap(Session.class);
                    if (session.isOpen()) {
                        session.doWork(connection -> {
                            try (Statement statement = connection.createStatement()) {
                                statement.execute("RESET ROLE");
                                log.debug("Rol de BD reseteado al usuario por defecto.");
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("Error al resetear el rol de BD", e);
                }
            }
        }
    }
}
