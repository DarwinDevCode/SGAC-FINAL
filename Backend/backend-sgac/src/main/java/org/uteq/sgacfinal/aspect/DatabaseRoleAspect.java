package org.uteq.sgacfinal.aspect;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import java.sql.Statement;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseRoleAspect {
    private final EntityManager entityManager;

    @Around("execution(* org.uteq.sgacfinal.repository.*.*(..))")
    public Object applyDatabaseRole(ProceedingJoinPoint joinPoint) throws Throwable {
        boolean roleChanged = false;


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