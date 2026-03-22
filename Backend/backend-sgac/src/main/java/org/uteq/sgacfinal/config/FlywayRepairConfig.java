package org.uteq.sgacfinal.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Ejecuta automáticamente flyway.repair() antes de que Spring llame a migrate().
 * Esto repara los checksums que no coinciden (mismatch) entre los archivos SQL locales
 * y lo que está registrado en la tabla flyway_schema_history de la base de datos,
 * evitando que la aplicación falle al iniciar.
 */
@Component
public class FlywayRepairConfig implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof Flyway flyway) {
            flyway.repair();
        }
        return bean;
    }
}
