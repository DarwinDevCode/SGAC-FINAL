package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.NotificationRequest;
import org.uteq.sgacfinal.entity.Ayudantia;
import org.uteq.sgacfinal.entity.InformeMensual;
import org.uteq.sgacfinal.entity.PeriodoFase;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.repository.InformeMensualRepository;
import org.uteq.sgacfinal.repository.configuracion.ICronogramaRepository;
import org.uteq.sgacfinal.service.INotificacionService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InformeMensualAlertJob {

    private final AyudantiaRepository ayudantiaRepository;
    private final InformeMensualRepository informeRepository;
    private final ICronogramaRepository periodoFaseRepository;
    private final INotificacionService notificacionService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void verificarFaseUltimaDeCierre() {
        log.info("Iniciando tarea programada: verificarFaseUltimaDeCierre para alertas preventivas de Informes");
        LocalDate hoy = LocalDate.now();
        LocalDate dosDiasDespues = hoy.plusDays(2);

        List<PeriodoFase> fases = periodoFaseRepository.findAll().stream()
                .filter(f -> f.getFechaFin().equals(dosDiasDespues))
                .filter(f -> f.getIdTipoFase().getNombre().toUpperCase().contains("CIERRE") ||
                        f.getIdTipoFase().getNombre().toUpperCase().contains("EVALUACION"))
                .toList();

        if (fases.isEmpty()) {
            log.info("No hay fases de cierre que terminen en 2 días.");
            return;
        }

        for (PeriodoFase fase : fases) {
            Integer idPeriodo = fase.getIdPeriodoAcademico().getIdPeriodoAcademico();

            List<Ayudantia> ayudantiasActivas = ayudantiaRepository.findAll().stream()
                    .filter(a -> a.getPostulacion().getConvocatoria().getPeriodoAcademico().getIdPeriodoAcademico().equals(idPeriodo))
                    .toList();

            for (Ayudantia ayudantia : ayudantiasActivas) {
                Integer mes = hoy.getMonthValue();
                Integer anio = hoy.getYear();

                Optional<InformeMensual> informeOpt = informeRepository.findByAyudantia_IdAyudantiaAndMesAndAnio(ayudantia.getIdAyudantia(), mes, anio);

                if (informeOpt.isEmpty() ||
                        List.of("NO_INICIADO", "EN_ELABORACION").contains(informeOpt.get().getTipoEstadoInforme().getCodigo())) {

                    String nombreAyudante = ayudantia.getPostulacion().getEstudiante().getUsuario().getNombres() + " " +
                            ayudantia.getPostulacion().getEstudiante().getUsuario().getApellidos();

                    notificacionService.enviarNotificacion(
                            ayudantia.getPostulacion().getEstudiante().getUsuario().getIdUsuario(),
                            NotificationRequest.builder()
                                    .titulo("Alerta Preventiva ÚLTIMOS DÍAS para envío de Informe Mensual")
                                    .mensaje("Hola " + nombreAyudante + ", te quedan 2 días para el cierre del periodo. " +
                                            "Recuerda generar y enviar tu Informe Mensual para evitar el estado REZAGADO. " +
                                            "Enlace directo al módulo: /ayudante/informes")
                                    .tipo("WARNING")
                                    .build()
                    );
                }
            }
        }
    }
}
