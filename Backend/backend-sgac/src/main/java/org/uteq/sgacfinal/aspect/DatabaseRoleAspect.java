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
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uteq.sgacfinal.config.UserContext;

import java.sql.Statement;

@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DatabaseRoleAspect {
    private final EntityManager entityManager;

    //@Around("execution(* org.uteq.sgacfinal.repository.*.*(..))")
    @Around("execution(* org.uteq.sgacfinal.service.*.*(..))")
    public Object applyDatabaseRole(ProceedingJoinPoint joinPoint) throws Throwable {
        String appRole = UserContext.getAppRole();
        boolean roleChanged = false;

        boolean txActiveBefore = TransactionSynchronizationManager.isActualTransactionActive();
        log.debug("[DB-ROLE] -> {} txActive={} thread={}", joinPoint.getSignature(), txActiveBefore, Thread.currentThread().getName());

        if (appRole != null && !appRole.isEmpty()) {
            String dbRole = appRole.toLowerCase().startsWith("role_")
                    ? appRole.toLowerCase()
                    : "role_" + appRole.toLowerCase();

            // SET LOCAL ROLE solo tiene efecto dentro de una transacción.
            // Si acá no hay tx activa, no intentamos cambiar el rol para evitar inconsistencias.
            if (txActiveBefore) {
                try {
                    Session session = entityManager.unwrap(Session.class);
                    session.doWork(connection -> {
                        try (Statement statement = connection.createStatement()) {
                            statement.execute("SET LOCAL ROLE " + dbRole);
                            log.debug("[DB-ROLE] Rol de BD cambiado exitosamente a: {}", dbRole);
                        }
                    });
                    roleChanged = true;
                } catch (Exception e) {
                    log.error("[DB-ROLE] No se pudo hacer SET LOCAL ROLE en la base de datos", e);
                }
            } else {
                log.debug("[DB-ROLE] Sin tx activa; se omite SET LOCAL ROLE para dbRole={}", dbRole);
            }
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (roleChanged) {
                boolean txActiveFinally = TransactionSynchronizationManager.isActualTransactionActive();
                try {
                    // RESET ROLE debe ejecutarse bajo la misma tx/conexión; si ya no hay tx,
                    // evitar tocar conexión porque puede provocar 'No active transaction' en distintos proveedores.
                    if (txActiveFinally) {
                        Session session = entityManager.unwrap(Session.class);
                        if (session.isOpen()) {
                            session.doWork(connection -> {
                                try (Statement statement = connection.createStatement()) {
                                    statement.execute("RESET ROLE");
                                    log.debug("[DB-ROLE] Rol de BD reseteado al usuario por defecto.");
                                }
                            });
                        }
                    } else {
                        log.debug("[DB-ROLE] Ya no hay tx activa; se omite RESET ROLE");
                    }
                } catch (Exception e) {
                    log.error("[DB-ROLE] Error al resetear el rol de BD", e);
                }
            }

            log.debug("[DB-ROLE] <- {}", joinPoint.getSignature());
        }
    }
}
