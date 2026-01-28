package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.repository.IPermisoRepository;
import org.uteq.sgacfinal.service.IPermisoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermisoServiceImpl implements IPermisoService {

    private final IPermisoRepository permisoRepository;

    @Override
    public List<PermisoDTO> obtenerPermisos() {
        return permisoRepository.obtenerPermisosActuales()
                .stream()
                .map(row -> PermisoDTO.builder()
                        .rol((String) row[0])
                        .objeto((String) row[1])
                        .permiso((String) row[2])
                        .build())
                .collect(Collectors.toList());
    }
}
