package org.uteq.sgacfinal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.RegistroActividad;
import org.uteq.sgacfinal.repository.RegistroActividadRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ValidacionActividadService {

    private final RegistroActividadRepository repository;

    @Transactional(readOnly = true)
    public void validarHorasSemanales(Integer idAyudantia, LocalDate fecha, BigDecimal horasNuevas) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int semana = fecha.get(weekFields.weekOfWeekBasedYear());
        int anio = fecha.get(weekFields.weekBasedYear());

        List<RegistroActividad> actividades = repository.findByAyudantiaIdAyudantia(idAyudantia);
        
        BigDecimal totalHorasSemana = actividades.stream()
                .filter(a -> a.getFecha().get(weekFields.weekOfWeekBasedYear()) == semana &&
                            a.getFecha().get(weekFields.weekBasedYear()) == anio)
                .map(RegistroActividad::getHorasDedicadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalHorasSemana.add(horasNuevas).compareTo(new BigDecimal("20.00")) > 0) {
            throw new RuntimeException("La planificación supera el límite de 20 horas semanales permitido.");
        }
    }
}
