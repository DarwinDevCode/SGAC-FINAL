package org.uteq.sgacfinal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.Ayudantia;
import org.uteq.sgacfinal.entity.InformeMensual;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.repository.InformeMensualRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformeMensualService {

    private final InformeMensualRepository repository;
    private final AyudantiaRepository ayudantiaRepository;

    @Transactional
    public InformeMensual generarInforme(Integer idAyudantia, Integer mes, Integer anio) {
        Ayudantia ayudantia = ayudantiaRepository.findById(idAyudantia)
                .orElseThrow(() -> new RuntimeException("Ayudantia no encontrada"));

        // Si ya existe un informe para ese mes/año, retornarlo
        return repository.findByAyudantiaIdAyudantiaAndMesAndAnio(idAyudantia, mes, anio)
                .orElseGet(() -> {
                    InformeMensual informe = InformeMensual.builder()
                            .ayudantia(ayudantia)
                            .mes(mes)
                            .anio(anio)
                            .estado("GENERADO")
                            .fechaGeneracion(LocalDateTime.now())
                            .build();
                    return repository.save(informe);
                });
    }

    @Transactional(readOnly = true)
    public List<InformeMensual> listarPorAyudantia(Integer idAyudantia) {
        return repository.findByAyudantiaIdAyudantiaOrderByAnioDescMesDesc(idAyudantia);
    }

    @Transactional(readOnly = true)
    public List<InformeMensual> listarPendientesDocente(Integer idDocente) {
        // Informes en estado GENERADO para las ayudantías del docente
        return repository.findByEstadoAndAyudantiaPostulacionConvocatoriaDocenteIdDocente("GENERADO", idDocente);
    }

    @Transactional(readOnly = true)
    public List<InformeMensual> listarPendientesCoordinador(Integer idCoordinador) {
        // Informes ya revisados por el docente, pendientes de aprobación del coordinador
        return repository.findByEstado("REVISADO_DOCENTE");
    }

    @Transactional
    public InformeMensual revisionDocente(Integer idInforme, String observaciones) {
        InformeMensual informe = repository.findById(idInforme)
                .orElseThrow(() -> new RuntimeException("Informe no encontrado"));

        informe.setEstado("REVISADO_DOCENTE");
        informe.setFechaRevisionDocente(LocalDateTime.now());
        informe.setObservaciones(observaciones);

        return repository.save(informe);
    }

    @Transactional
    public InformeMensual rechazarDocente(Integer idInforme, String observaciones) {
        InformeMensual informe = repository.findById(idInforme)
                .orElseThrow(() -> new RuntimeException("Informe no encontrado"));

        informe.setEstado("RECHAZADO_DOCENTE");
        informe.setFechaRevisionDocente(LocalDateTime.now());
        informe.setObservaciones(observaciones);

        return repository.save(informe);
    }

    @Transactional
    public InformeMensual aprobacionCoordinador(Integer idInforme) {
        InformeMensual informe = repository.findById(idInforme)
                .orElseThrow(() -> new RuntimeException("Informe no encontrado"));

        if (!"REVISADO_DOCENTE".equals(informe.getEstado())) {
            throw new RuntimeException("El informe debe ser revisado por el docente antes de la aprobación final.");
        }

        informe.setEstado("APROBADO_COORDINADOR");
        informe.setFechaAprobacionCoordinador(LocalDateTime.now());

        return repository.save(informe);
    }

    @Transactional
    public InformeMensual rechazarCoordinador(Integer idInforme, String observaciones) {
        InformeMensual informe = repository.findById(idInforme)
                .orElseThrow(() -> new RuntimeException("Informe no encontrado"));

        informe.setEstado("RECHAZADO_COORDINADOR");
        informe.setObservaciones(observaciones);

        return repository.save(informe);
    }
}
