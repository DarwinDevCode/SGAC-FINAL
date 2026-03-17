package org.uteq.sgacfinal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.Docente;
import org.uteq.sgacfinal.entity.EvaluacionDesempeno;
import org.uteq.sgacfinal.entity.RegistroActividad;
import org.uteq.sgacfinal.repository.DocenteRepository;
import org.uteq.sgacfinal.repository.EvaluacionDesempenoRepository;
import org.uteq.sgacfinal.repository.RegistroActividadRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EvaluacionDesempenoService {

    private final EvaluacionDesempenoRepository repository;
    private final RegistroActividadRepository registroActividadRepository;
    private final DocenteRepository docenteRepository;

    @Transactional
    public EvaluacionDesempeno evaluarSesion(Integer idRegistroActividad, Integer idDocente, Integer puntaje, String retroalimentacion) {
        RegistroActividad registro = registroActividadRepository.findById(idRegistroActividad)
                .orElseThrow(() -> new RuntimeException("Registro de actividad no encontrado"));

        Docente docente = docenteRepository.findById(idDocente)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));

        EvaluacionDesempeno evaluacion = repository.findByRegistroActividadIdRegistroActividad(idRegistroActividad)
                .orElse(new EvaluacionDesempeno());

        evaluacion.setRegistroActividad(registro);
        evaluacion.setDocente(docente);
        evaluacion.setPuntaje(puntaje);
        evaluacion.setRetroalimentacion(retroalimentacion);
        evaluacion.setFechaEvaluacion(LocalDateTime.now());

        return repository.save(evaluacion);
    }
}
