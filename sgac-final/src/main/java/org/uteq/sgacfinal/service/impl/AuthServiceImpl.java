package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.dto.TipoRolDTO;
import org.uteq.sgacfinal.dto.UsuarioDTO;
import org.uteq.sgacfinal.entity.AyudanteCatedra;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IAuthService;
import org.uteq.sgacfinal.service.IEstudianteService;
import org.uteq.sgacfinal.service.IUsuarioService;
import org.uteq.sgacfinal.service.IUsuarioTipoRolService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {
    private final IAuthRepository authRepository;
    private final IUsuarioService usuarioService;
    private final IUsuarioTipoRolService rolService;
    private final IEstudianteService estudianteService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioTipoRolRepository usuarioTipoRolRepository;

    private final EstudianteRepository estudianteRepository;
    private final DocenteRepository docenteRepository;
    private final DecanoRepository decanoRepository;
    private final CoordinadorRepository coordinadorRepository;
    private final AyudanteCatedraRepository ayudanteCatedraRepository;

    @Override
    public UsuarioDTO login(String usuario, String contrasenia) {
        Supplier<List<Object[]>> loginSupplier = () ->
                authRepository.login(usuario, contrasenia);

        Consumer<List<Object[]>> validarCredenciales = result -> {
            if (result.isEmpty())
                throw new RuntimeException("Credenciales incorrectas");
        };

        Consumer<Object[]> validarUsuario = row -> {
            if (row[4] == null)
                throw new RuntimeException("Usuario inválido");
        };

        Consumer<Object[]> validarRoles = row -> {
            if (row[5] == null || row[5].toString().isBlank()) {
                throw new RuntimeException("Usuario sin roles asignados");
            }
        };

        Consumer<Object[]> validaciones =
                validarUsuario.andThen(validarRoles);

        List<Object[]> result = loginSupplier.get();
        validarCredenciales.accept(result);

        Object[] row = result.get(0);
        validaciones.accept(row);

        List<TipoRolDTO> roles = List.of(row[5].toString().split(","))
                .stream()
                .map(r -> TipoRolDTO.builder()
                        .nombreTipoRol(r)
                        .build())
                .toList();

        return UsuarioDTO.builder()
                .idUsuario((Integer) row[0])
                .nombres((String) row[1])
                .apellidos((String) row[2])
                .correo((String) row[3])
                .nombreUsuario((String) row[4])
                .roles(roles)
                .build();


//        List<Object[]> result = authRepository.login(usuario, contrasenia);
//
//        if (result.isEmpty()) {
//            throw new RuntimeException("Credenciales incorrectas");
//        }
//
//        Object[] row = result.get(0);
//
//        List<TipoRolDTO> roles = row[5] == null
//                ? List.of()
//                : List.of(row[5].toString().split(","))
//                .stream()
//                .map(r -> TipoRolDTO.builder()
//                        .nombreTipoRol(r)
//                        .build())
//                .toList();
//
//        return UsuarioDTO.builder()
//                .idUsuario((Integer) row[0])
//                .nombres((String) row[1])
//                .apellidos((String) row[2])
//                .correo((String) row[3])
//                .nombreUsuario((String) row[4])
//                .roles(roles)
//                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EstudianteResponseDTO registrarEstudiante(RegistroEstudianteRequestDTO request) {
        Integer idUsuarioResponse = usuarioRepository.registrarUsuario(
                request.getNombres(),
                request.getApellidos(),
                request.getCedula(),
                request.getCorreo(),
                request.getNombreUsuario(),
                request.getContrasenia()
        );

        if (idUsuarioResponse == -1)
            throw new RuntimeException("Error al crear el usuario. Verifique cédula, correo o nombre de usuario duplicados.");

        Integer resultadoRol = usuarioTipoRolRepository.asignarRolUsuario(
                idUsuarioResponse,
                1
        );

        if (resultadoRol == -1)
            throw new RuntimeException("Error al asignar el rol de estudiante.");

        Integer idEstudianteResponse = estudianteRepository.registrarEstudiante(
                idUsuarioResponse,
                request.getIdCarrera(),
                request.getMatricula(),
                request.getSemestre(),
                "ACTIVO"
        );

        if (idEstudianteResponse == -1)
            throw new RuntimeException("Error al crear la ficha de estudiante. Verifique si la matrícula ya existe.");

        return EstudianteResponseDTO.builder()
                .idEstudiante(idEstudianteResponse)
                .idUsuario(idUsuarioResponse)
                .nombreCompletoUsuario(request.getNombres() + " " + request.getApellidos())
                .cedula(request.getCedula())
                .correo(request.getCorreo())
                .idCarrera(request.getIdCarrera())
                .matricula(request.getMatricula())
                .semestre(request.getSemestre())
                .estadoAcademico("ACTIVO")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DocenteResponseDTO registrarDocente(RegistroDocenteRequestDTO request) {
        Integer idUsuarioResponse = usuarioRepository.registrarUsuario(
                request.getNombres(),
                request.getApellidos(),
                request.getCedula(),
                request.getCorreo(),
                request.getNombreUsuario(),
                request.getContrasenia()
        );

        if(idUsuarioResponse == -1)
            throw new RuntimeException("Error al crear el usuario. Verifique cédula, correo o nombre de usuario duplicados. " + idUsuarioResponse);

        Integer idUsuarioTipoRolResponse = usuarioTipoRolRepository.asignarRolUsuario(
                idUsuarioResponse,
                2
        );

        if(idUsuarioTipoRolResponse == -1)
            throw new RuntimeException("Error al asignar el rol de docente.");

        Integer idDocenteResponse = docenteRepository.registrarDocente(
                idUsuarioResponse,
                request.getFechaInicio()
        );

        if(idDocenteResponse == -1)
            throw new RuntimeException("Error al crear el docente.");

        return DocenteResponseDTO.builder()
                .idDocente(idDocenteResponse)
                .idUsuario(idUsuarioResponse)
                .nombreCompletoUsuario(request.getNombres()  + " " + request.getApellidos())
                .correoUsuario(request.getCorreo())
                .fechaInicio(request.getFechaInicio())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoordinadorResponseDTO registrarCoordinador(RegistroCoordinadorRequestDTO request) {
        Integer idUsuarioResponse = usuarioRepository.registrarUsuario(
                request.getNombres(),
                request.getApellidos(),
                request.getCedula(),
                request.getCorreo(),
                request.getNombreUsuario(),
                request.getContrasenia()
        );

        if(idUsuarioResponse == -1)
            throw new RuntimeException("Error al crear el usuario. Verifique cédula, correo o nombre de usuario duplicados. " + idUsuarioResponse);

        Integer idUsuarioTipoRolResponse = usuarioTipoRolRepository.asignarRolUsuario(
                idUsuarioResponse,
                3
        );

        if(idUsuarioTipoRolResponse == -1)
            throw new RuntimeException("Error al asignar el rol de coordinador.");

        Integer idCoordinadorResponse = coordinadorRepository.registrarCoordinador(
                idUsuarioResponse,
                request.getIdCarrera(),
                request.getFechaInicio(),
                request.getFechaFin()
        );

        if(idCoordinadorResponse == -1)
            throw new RuntimeException("Error al crear el coordinador.");

        return CoordinadorResponseDTO.builder()
                .idCoordinador(idCoordinadorResponse)
                .idUsuario(idUsuarioResponse)
                .nombreCompletoUsuario(request.getNombres()  + " " + request.getApellidos())
                .cedula(request.getCedula())
                .idCarrera(request.getIdCarrera())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DecanoResponseDTO registrarDecano(RegistroDecanoRequestDTO request) {
        Integer idUsuarioResponse = usuarioRepository.registrarUsuario(
                request.getNombres(),
                request.getApellidos(),
                request.getCedula(),
                request.getCorreo(),
                request.getNombreUsuario(),
                request.getContrasenia()
        );

        if(idUsuarioResponse == -1)
            throw new RuntimeException("Error al crear el usuario. Verifique cédula, correo o nombre de usuario duplicados. " + idUsuarioResponse);

        Integer idUsuarioTipoRolResponse = usuarioTipoRolRepository.asignarRolUsuario(
                idUsuarioResponse,
                4
        );

        if(idUsuarioTipoRolResponse == -1)
            throw new RuntimeException("Error al asignar el rol de decano.");

        Integer idDecanoResponse = decanoRepository.registrarDecano(
                idUsuarioResponse,
                request.getIdFacultad(),
                request.getFechaInicioGestion(),
                request.getFechaFinGestion()
        );

        if(idDecanoResponse == -1)
            throw new RuntimeException("Error al crear el decano.");

        return DecanoResponseDTO.builder()
                .idDecano(idDecanoResponse)
                .idUsuario(idUsuarioResponse)
                .nombreCompletoUsuario(request.getNombres()  + " " + request.getApellidos())
                .idFacultad(request.getIdFacultad())
                .fechaInicioGestion(request.getFechaInicioGestion())
                .fechaFinGestion(request.getFechaFinGestion())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AyudanteCatedraResponseDTO registrarAyudanteCatedra(RegistroAyudanteCatedraRequestDTO request) {
        Integer idUsuarioResponse = usuarioRepository.registrarUsuario(
                request.getNombres(),
                request.getApellidos(),
                request.getCedula(),
                request.getCorreo(),
                request.getNombreUsuario(),
                request.getContrasenia()
        );

        if(idUsuarioResponse == -1)
            throw new RuntimeException("Error al crear el usuario. Verifique cédula, correo o nombre de usuario duplicados. " + idUsuarioResponse);

        Integer idUsuarioTipoRolResponse = usuarioTipoRolRepository.asignarRolUsuario(
                idUsuarioResponse,
                5
        );

        if(idUsuarioTipoRolResponse == -1)
            throw new RuntimeException("Error al asignar el rol de ayudante de cátedra.");

        Integer idAyudanteCatedraResponse = ayudanteCatedraRepository.registrarAyudante(
                idUsuarioResponse,
                BigDecimal.valueOf(request.getHorasAyudante())
        );

        return AyudanteCatedraResponseDTO.builder()
                .idAyudanteCatedra(idAyudanteCatedraResponse)
                .idUsuario(idUsuarioResponse)
                .nombreCompletoUsuario(request.getNombres()  + " " + request.getApellidos())
                .cedulaUsuario(request.getCedula())
                .horasAyudante(BigDecimal.valueOf(request.getHorasAyudante()))
                .build();
    }

    @Override
    public Integer RegistrarUsuario(UsuarioRequestDTO request) {
        return 0;
    }


}
