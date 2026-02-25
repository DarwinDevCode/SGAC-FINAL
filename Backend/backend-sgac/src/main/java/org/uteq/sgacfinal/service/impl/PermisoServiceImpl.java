package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.Request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.repository.IPermisoRepository;
import org.uteq.sgacfinal.service.IPermisoService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    @Override
    public List<String> listarEsquemas() {
        return permisoRepository.listarEsquemas();
    }

    @Override
    public List<TipoObjetoResponseDTO> listarTiposObjeto() {
        List<Object[]> rawData = permisoRepository.listarTiposObjetoSeguridad();
        return rawData.stream()
                .map(row -> new TipoObjetoResponseDTO(
                        (Integer) row[0],
                        (String) row[1]
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listarElementos(String esquema, String tipoObjeto) {
        return permisoRepository.listarElementosPorTipo(esquema, tipoObjeto.toUpperCase());
    }

    @Override
    public List<PrivilegioResponseDTO> listarPrivilegios(Integer idTipoObjeto) {
        List<Object[]> rawData = permisoRepository.listarPrivilegiosPorTipoObjeto(idTipoObjeto);
        return rawData.stream()
                .map(row -> new PrivilegioResponseDTO(
                        (Integer) row[0],
                        (String) row[1],
                        (String) row[2]
                ))
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultadoMasivoResponseDTO gestionarPermisosMasivo(List<GestionPermisosRequestDTO> permisos) {
        log.info("Iniciando procesamiento masivo de {} permisos", permisos.size());
        int contadorExitos = 0;

        for (int i = 0; i < permisos.size(); i++) {
            GestionPermisosRequestDTO permiso = permisos.get(i);

            log.debug("[{}/{}] Procesando: Elemento={}, Privilegio={}, Otorgar={}",
                    (i + 1), permisos.size(), permiso.getElemento(), permiso.getPrivilegio(), permiso.getOtorgar());

            try {
                Boolean resultado = permisoRepository.gestionarPermisoElementoRaw(
                        permiso.getRolBd(),
                        permiso.getEsquema(),
                        permiso.getElemento(),
                        permiso.getCategoria(),
                        permiso.getPrivilegio(),
                        permiso.getOtorgar()
                );

                if (resultado != null && resultado) {
                    contadorExitos++;
                } else {
                    log.error("La función de BD devolvió FALSE para: {}", permiso.getElemento());
                    throw new RuntimeException("Error en BD al procesar: " + permiso.getElemento());
                }
            } catch (Exception e) {
                log.error("Excepción al procesar permiso index {}: {}", i, e.getMessage());
                throw e; // Lanza para hacer rollback
            }
        }

        log.info("Procesamiento masivo finalizado. Exitosos: {}", contadorExitos);

        return ResultadoMasivoResponseDTO.builder()
                .totalProcesados(permisos.size())
                .exitosos(contadorExitos)
                .fallidos(0)
                .exito(true)
                .mensaje("Éxito total: Se procesaron los " + permisos.size() + " permisos correctamente.")
                .build();
    }


//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public ResultadoMasivoResponseDTO gestionarPermisosMasivo(List<GestionPermisosRequestDTO> permisos) {
//
//        for (GestionPermisosRequestDTO permiso : permisos) {
//            Boolean resultado = permisoRepository.gestionarPermisoElementoRaw(
//                    permiso.getRolBd(),
//                    permiso.getEsquema(),
//                    permiso.getElemento(),
//                    permiso.getCategoria(),
//                    permiso.getPrivilegio(),
//                    permiso.getOtorgar()
//            );
//
//            if (resultado == null || !resultado) {
//                throw new RuntimeException("Fallo atómico: Error al procesar el elemento [" +
//                        permiso.getElemento() + "] con el privilegio [" + permiso.getPrivilegio() + "]. " +
//                        "Toda la operación ha sido revertida.");
//            }
//        }
//
//        return ResultadoMasivoResponseDTO.builder()
//                .totalProcesados(permisos.size())
//                .exitosos(permisos.size())
//                .fallidos(0)
//                .exito(true)
//                .mensaje("Éxito total: Se procesaron los " + permisos.size() + " permisos correctamente.")
//                .build();
//    }


//    @Override
//    @Transactional
//    public ResultadoMasivoResponseDTO gestionarPermisosMasivo(List<GestionPermisosRequestDTO> permisos) {
//        List<DetalleResultadoDTO> detalles = new ArrayList<>();
//        int exitosos = 0;
//        int fallidos = 0;
//
//        for (GestionPermisosRequestDTO permiso : permisos) {
//            Boolean resultado = permisoRepository.gestionarPermisoElementoRaw(
//                    permiso.getRolBd(),
//                    permiso.getEsquema(),
//                    permiso.getElemento(),
//                    permiso.getCategoria(),
//                    permiso.getPrivilegio(),
//                    permiso.getOtorgar()
//            );
//
//            if (resultado) {
//                detalles.add(DetalleResultadoDTO.builder()
//                        .elemento(permiso.getElemento())
//                        .privilegio(permiso.getPrivilegio())
//                        .exito(true)
//                        .mensaje("Permiso " + (permiso.getOtorgar() ? "otorgado" : "revocado") + " exitosamente.")
//                        .build());
//                exitosos++;
//            } else {
//                detalles.add(DetalleResultadoDTO.builder()
//                        .elemento(permiso.getElemento())
//                        .privilegio(permiso.getPrivilegio())
//                        .exito(false)
//                        .mensaje("Error desconocido al gestionar permiso.")
//                        .build());
//                fallidos++;
//            }
//        }
//
//        return ResultadoMasivoResponseDTO.builder()
//                .totalProcessados(permisos.size())
//                .exitosos(exitosos)
//                .fallidos(fallidos)
//                .detalles(detalles)
//                .exito(fallidos == 0)
//                .mensaje(fallidos == 0 ?
//                        "Todos los " + exitosos + " permisos procesados exitosamente." :
//                        "Completado: " + exitosos + " exitosos, " + fallidos + " fallidos.")
//                .build();
//    }



//    @Override
//    @Transactional(readOnly = true)
//    public List<ElementoBdResponseDTO> listarElementos(String esquema, String categoria) {
//        List<String> nombres = permisoRepository.listarElementosPorFiltroRaw(esquema, categoria);
//
//        return nombres.stream()
//                .map(nombre -> new ElementoBdResponseDTO(nombre))
//                .collect(Collectors.toList());
//    }

//    @Override
//    public List<EsquemaResponseDTO> listarEsquemas() {
//        return permisoRepository.listarEsquemas();
//    }
}
