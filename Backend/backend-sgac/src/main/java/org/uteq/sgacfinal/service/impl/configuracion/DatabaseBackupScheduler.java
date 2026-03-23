package org.uteq.sgacfinal.service.impl.configuracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseBackupScheduler {

    private final DatabaseBackupService backupService;
    //@Scheduled(cron = "0 * * * * *")
    //@Scheduled(cron = "0 0 2 1,15 * *")
    //@Scheduled(fixedRate = 300000)
    //public void ejecutarRespaldoProgramado() {
    //    log.info(">>> INICIANDO RESPALDO AUTOMÁTICO <<<");
    //    try {
    //        var resultado = backupService.generarRespaldo();
    //        if (resultado.valido()) log.info("Respaldo automático completado con éxito: {}", resultado.datos());
    //        else log.error("Fallo en el respaldo automático: {}", resultado.mensaje());
    //    } catch (Exception e) {
    //        log.error("Error crítico en la tarea programada de respaldo", e);
    //    }
    //}
}