package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.CertificadoRequestDTO;
import org.uteq.sgacfinal.dto.Response.CertificadoResponseDTO;
import org.uteq.sgacfinal.entity.Certificado;
import org.uteq.sgacfinal.repository.CertificadoRepository;
import org.uteq.sgacfinal.service.ICertificadoService;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificadoServiceImpl implements ICertificadoService {

    private final CertificadoRepository certificadoRepository;

    @Override
    public CertificadoResponseDTO crear(CertificadoRequestDTO request) {
        Integer idGenerado = certificadoRepository.registrarCertificado(
                request.getIdAyudantia(),
                request.getIdUsuario(),
                request.getCodigoVerificacion(),
                request.getFechaEmision(),
                request.getTotalHorasCertificadas(),
                request.getArchivo(),
                request.getEstado()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al generar el certificado.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public CertificadoResponseDTO actualizar(Integer id, CertificadoRequestDTO request) {
        Integer resultado = certificadoRepository.actualizarCertificado(
                id,
                request.getEstado(),
                request.getArchivo()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar el certificado.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = certificadoRepository.desactivarCertificado(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar el certificado.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CertificadoResponseDTO buscarPorId(Integer id) {
        Certificado cert = certificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificado no encontrado con ID: " + id));
        return mapearADTO(cert);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CertificadoResponseDTO> listarPorUsuario(Integer idUsuario) {
        return certificadoRepository.findById(idUsuario).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificadoResponseDTO> listarTodosActivos() {
        List<Object[]> resultados = certificadoRepository.listarCertificadosActivosSP();
        return resultados.stream()
                .map(this::mapearDesdeObjectArray)
                .collect(Collectors.toList());
    }

    private CertificadoResponseDTO mapearADTO(Certificado entidad) {
        String nombreUsuario = "";
        if(entidad.getUsuario() != null) {
            nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        }

        return CertificadoResponseDTO.builder()
                .idCertificado(entidad.getIdCertificado())
                .idAyudantia(entidad.getAyudantia().getIdAyudantia())
                .idUsuario(entidad.getUsuario() != null ? entidad.getUsuario().getIdUsuario() : null)
                .nombreUsuarioEmisor(nombreUsuario)
                .codigoVerificacion(entidad.getCodigoVerificacion())
                .fechaEmision(entidad.getFechaEmision())
                .totalHorasCertificadas(entidad.getTotalHorasCertificadas())
                .estado(entidad.getEstado())
                .activo(entidad.getActivo())
                .build();
    }

    private CertificadoResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return CertificadoResponseDTO.builder()
                .idCertificado((Integer) obj[0])
                .codigoVerificacion((String) obj[1])
                .fechaEmision(obj[2] != null ? ((Date) obj[2]).toLocalDate() : null)
                .totalHorasCertificadas((Integer) obj[3])
                .estado((String) obj[4])
                .activo(true)
                .build();
    }
}