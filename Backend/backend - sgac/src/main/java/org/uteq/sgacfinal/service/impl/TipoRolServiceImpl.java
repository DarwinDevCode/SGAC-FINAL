package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import org.uteq.sgacfinal.entity.TipoRol;
import org.uteq.sgacfinal.repository.ITipoRolRepository;
import org.uteq.sgacfinal.service.ITipoRolService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoRolServiceImpl implements ITipoRolService {
    private final ITipoRolRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoRolResponseDTO> listarTodos() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRolResponseDTO> listarActivos() {
        return repository.findByActivoTrue().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public TipoRolResponseDTO crear(TipoRolRequestDTO request) {
        Integer id = repository.crearTipoRol(request.getNombreTipoRol());
        return repository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Override
    @Transactional
    public TipoRolResponseDTO actualizar(Integer id, TipoRolRequestDTO request) {
        repository.actualizarTipoRol(id, request.getNombreTipoRol());
        return repository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        repository.desactivarTipoRol(id);
    }

    private TipoRolResponseDTO mapToDTO(TipoRol entity) {
        return TipoRolResponseDTO.builder()
                .idTipoRol(entity.getIdTipoRol())
                .nombreTipoRol(entity.getNombreTipoRol())
                .activo(entity.getActivo())
                .build();
    }


    @Transactional
    public void sincronizarPermisos(GestionPermisosRequestDTO request) {
        for (GestionPermisosRequestDTO.PermisoDetalleDTO permiso : request.getPermisos()) {

            Boolean exito = repository.gestionarPermisoRol(
                    request.getNombreRol(),
                    request.getEsquema(),
                    request.getTabla(),
                    permiso.getPrivilegio(),
                    permiso.isOtorgar()
            );

            if (exito == null || !exito)
                throw new RuntimeException("Error al gestionar el permiso " + permiso.getPrivilegio() + " para la tabla " + request.getTabla());
        }
    }










//    private final ITipoRolRepository ITipoRolRepository;
//
//    @Override
//    public TipoRolResponseDTO crear(TipoRolRequestDTO request) {
//        TipoRol entidad = new TipoRol();
//        entidad.setNombreTipoRol(request.getNombreTipoRol());
//        entidad.setActivo(true);
//
//        entidad = ITipoRolRepository.save(entidad);
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    public TipoRolResponseDTO actualizar(Integer id, TipoRolRequestDTO request) {
//        TipoRol entidad = ITipoRolRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
//
//        entidad.setNombreTipoRol(request.getNombreTipoRol());
//        if(request.getActivo() != null) entidad.setActivo(request.getActivo());
//
//        entidad = ITipoRolRepository.save(entidad);
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public TipoRolResponseDTO buscarPorId(Integer id) {
//        TipoRol entidad = ITipoRolRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public TipoRolResponseDTO buscarPorNombre(String nombre) {
//        TipoRol entidad = ITipoRolRepository.findByNombreTipoRol(nombre)
//                .orElseThrow(() -> new RuntimeException("Rol no encontrado con nombre: " + nombre));
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<TipoRolResponseDTO> listarTodos() {
//        return ITipoRolRepository.findAll().stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }
//
//    private TipoRolResponseDTO mapearADTO(TipoRol entidad) {
//        return TipoRolResponseDTO.builder()
//                .idTipoRol(entidad.getIdTipoRol())
//                .nombreTipoRol(entidad.getNombreTipoRol())
//                .activo(entidad.getActivo())
//                .build();
//    }
}