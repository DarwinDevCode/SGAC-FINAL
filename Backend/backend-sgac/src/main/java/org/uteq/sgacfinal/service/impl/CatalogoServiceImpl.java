package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.TipoEstadoAyudantiaResponse;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaResponse;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRegistroResponse;
import org.uteq.sgacfinal.dto.Response.TipoEvidenciaResponse;
import org.uteq.sgacfinal.repository.TipoEstadoAyudantiaRepository;
import org.uteq.sgacfinal.repository.TipoEstadoEvidenciaRepository;
import org.uteq.sgacfinal.repository.TipoEstadoRegistroRepository;
import org.uteq.sgacfinal.repository.TipoEvidenciaRepository;
import org.uteq.sgacfinal.service.ICatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoServiceImpl implements ICatalogoService {
    private final TipoEstadoRegistroRepository estadoRegistroRepo;
    private final TipoEstadoEvidenciaRepository estadoEvidenciaRepo;
    private final TipoEvidenciaRepository tipoEvidenciaRepo;
    private final TipoEstadoAyudantiaRepository estadoAyudantiaRepo;

    @Override
    @Transactional(readOnly = true)
    public List<TipoEstadoRegistroResponse> estadosRegistro() {
        return estadoRegistroRepo.findByActivoTrue().stream()
                .map(e -> TipoEstadoRegistroResponse.builder()
                        .id(e.getId())
                        .nombreEstado(e.getNombreEstado())
                        .descripcion(e.getDescripcion())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEstadoEvidenciaResponse> estadosEvidencia() {
        return estadoEvidenciaRepo.findByActivoTrue().stream()
                .map(e -> TipoEstadoEvidenciaResponse.builder()
                        .id(e.getId())
                        .nombreEstado(e.getNombreEstado())
                        .descripcion(e.getDescripcion())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEvidenciaResponse> tiposEvidencia() {
        return tipoEvidenciaRepo.findByActivoTrue().stream()
                .map(e -> TipoEvidenciaResponse.builder()
                        .id(e.getId())
                        .nombre(e.getNombre())
                        .extensionPermitida(e.getExtensionPermitida())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEstadoAyudantiaResponse> estadosAyudantia() {
        return estadoAyudantiaRepo.findByActivoTrue().stream()
                .map(e -> TipoEstadoAyudantiaResponse.builder()
                        .id(e.getId())
                        .nombreEstado(e.getNombreEstado())
                        .descripcion(e.getDescripcion())
                        .build())
                .toList();
    }

}
