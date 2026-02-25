package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.Request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Response.PermisoRolResponseDTO;
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

    @Override
    @Transactional(readOnly = true)
    public List<PermisoRolResponseDTO> consultarPermisos(FiltroPermisosRequestDTO filtro) {
        List<Object[]> resultadosRaw = permisoRepository.consultarPermisosRolRaw(
                filtro.getRolBd(),
                filtro.getEsquema(),
                filtro.getCategoria(),
                filtro.getPrivilegio()
        );

        return resultadosRaw.stream().map(row ->
                PermisoRolResponseDTO.builder()
                        .esquema((String) row[0])
                        .elemento((String) row[1])
                        .categoria((String) row[2])
                        .privilegio((String) row[3])
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Boolean gestionarPermiso(GestionPermisosRequestDTO request) {
        return permisoRepository.gestionarPermisoElementoRaw(
                request.getRolBd(),
                request.getEsquema(),
                request.getElemento(),
                request.getCategoria(),
                request.getPrivilegio(),
                request.getOtorgar()
        );
    }
}
