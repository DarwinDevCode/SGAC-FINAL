package org.uteq.sgacfinal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.Ayudantia;
import org.uteq.sgacfinal.entity.MensajeInterno;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.MensajeInternoRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComunicacionService {

    private final MensajeInternoRepository repository;
    private final org.uteq.sgacfinal.repository.AyudantiaRepository ayudantiaRepository;
    private final org.uteq.sgacfinal.repository.IUsuariosRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<org.uteq.sgacfinal.dto.Response.MensajeInternoResponseDTO> obtenerHistorial(Integer idAyudantia) {
        return repository.findByAyudantiaIdAyudantiaOrderByFechaEnvioAsc(idAyudantia).stream()
                .map(this::mapearADTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public org.uteq.sgacfinal.dto.Response.MensajeInternoResponseDTO enviarMensaje(MensajeInterno mensaje) {
        mensaje.setFechaEnvio(LocalDateTime.now());
        mensaje.setLeido(false);
        MensajeInterno guardado = repository.save(mensaje);
        
        // Cargar emisor si no está cargado (FetchType.LAZY)
        if (guardado.getEmisor() == null && mensaje.getEmisor() != null) {
            guardado.setEmisor(mensaje.getEmisor());
        }
        
        return mapearADTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<org.uteq.sgacfinal.dto.Response.MensajeInternoResponseDTO> buscarMensajes(Integer idAyudantia, String criterio) {
        // Búsqueda eficiente usando query nativa en BD (ILIKE) en lugar de filtrado en memoria
        return repository.buscarPorCriterio(idAyudantia, criterio).stream()
                .map(this::mapearADTO)
                .collect(java.util.stream.Collectors.toList());
    }

    private org.uteq.sgacfinal.dto.Response.MensajeInternoResponseDTO mapearADTO(MensajeInterno m) {
        String nombre = "Sistema";
        if (m.getEmisor() != null) {
            nombre = m.getEmisor().getNombres() + " " + m.getEmisor().getApellidos();
        }
        
        return org.uteq.sgacfinal.dto.Response.MensajeInternoResponseDTO.builder()
                .idMensajeInterno(m.getIdMensajeInterno())
                .idAyudantia(m.getAyudantia().getIdAyudantia())
                .idUsuarioEmisor(m.getEmisor() != null ? m.getEmisor().getIdUsuario() : null)
                .nombreEmisor(nombre)
                .mensaje(m.getMensaje())
                .fechaEnvio(m.getFechaEnvio())
                .rutaArchivoAdjunto(m.getRutaArchivoAdjunto())
                .leido(m.getLeido())
                .build();
    }

    @Transactional
    public void generarAlertaFaltaEvidencia(Integer idAyudantia, String sesionInfo) {
        Ayudantia ayu = ayudantiaRepository.findById(idAyudantia).orElse(null);
        Usuario sistema = usuarioRepository.findById(1).orElse(null);
        
        if (ayu != null && sistema != null) {
            MensajeInterno alerta = MensajeInterno.builder()
                    .ayudantia(ayu)
                    .emisor(sistema)
                    .mensaje("[ALERTA AUTOMÁTICA] La sesión '" + sesionInfo + "' ha sido registrada sin evidencia adjunta. Por favor, subir el respaldo necesario.")
                    .fechaEnvio(LocalDateTime.now())
                    .leido(false)
                    .build();
            repository.save(alerta);
        }
    }
}
