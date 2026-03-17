package org.uteq.sgacfinal.service.documentos.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.documentos.DocumentoInsertRequestDTO;
import org.uteq.sgacfinal.dto.Request.documentos.DocumentoUpdateRequestDTO;
import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.Response.documentos.*;
import org.uteq.sgacfinal.repository.documentos.DocumentoRepository;
import org.uteq.sgacfinal.service.cloudinary.CloudinaryUploadServiceImpl;
import org.uteq.sgacfinal.service.documentos.DocumentoService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final CloudinaryUploadServiceImpl cloudinaryService;

    @Override
    public RespuestaOperacionDTO<List<FacultadResponseDTO>> getFacultades() {
        return documentoRepository.getFacultades();
    }

    @Override
    public RespuestaOperacionDTO<List<CarreraResponseDTO>> getCarreras(Integer idFacultad) {
        return documentoRepository.getCarreras(idFacultad);
    }

    @Override
    public RespuestaOperacionDTO<List<TipoDocumentoResponseDTO>> getTiposDocumento() {
        return documentoRepository.getTiposDocumento();
    }

    @Override
    public RespuestaOperacionDTO<DocumentoIdResponseDTO> guardarDocumento(
            MultipartFile archivo, String nombre, Integer idTipo,
            Integer idUser, Integer idFac, Integer idCar) {

        Map<String, Object> cloudRes;
        try {
            cloudRes = cloudinaryService.upload(archivo);
        } catch (RuntimeException e) {
            return new RespuestaOperacionDTO<>(false, "Error al subir archivo a la nube: " + e.getMessage(), null);
        }

        String url = cloudRes.get("url").toString();
        String publicId = cloudRes.get("publicId").toString();
        String extension = cloudRes.get("extension") != null ? cloudRes.get("extension").toString() : "";
        Integer peso = ((Long) cloudRes.get("pesoBytes")).intValue();

        DocumentoInsertRequestDTO req = new DocumentoInsertRequestDTO(
                nombre, url, extension, peso, idTipo, idUser, idFac, idCar
        );

        RespuestaOperacionDTO<DocumentoIdResponseDTO> dbRes = documentoRepository.insertar(req);

        if (!dbRes.valido()) {
            try {
                log.warn("Error en DB. Revirtiendo subida de Cloudinary para public_id: {}", publicId);
                cloudinaryService.delete(publicId);
            } catch (IOException e) {
                log.error("¡CRÍTICO! No se pudo revertir la subida en Cloudinary: {}", publicId, e);
            }
        }

        return dbRes;
    }

    @Override
    public RespuestaOperacionDTO<Void> actualizarDocumento(DocumentoUpdateRequestDTO req) {
        return documentoRepository.actualizar(req);
    }

    @Override
    public RespuestaOperacionDTO<Void> eliminarDocumento(Integer idDocumento) {
        RespuestaOperacionDTO<DocumentoEliminadoResponseDTO> dbRes = documentoRepository.eliminar(idDocumento);

        if (dbRes.valido() && dbRes.datos() != null) {
            String url = dbRes.datos().ruta();
            String publicId = extraerPublicIdDeUrl(url);

            try {
                if (publicId != null) {
                    cloudinaryService.delete(publicId);
                    log.info("Archivo eliminado físicamente de Cloudinary: {}", publicId);
                }
            } catch (IOException e) {
                log.error("Error al eliminar archivo físico de Cloudinary para la URL: {}", url, e);
            }
        }

        return new RespuestaOperacionDTO<>(dbRes.valido(), dbRes.mensaje(), null);
    }

    @Override
    public RespuestaOperacionDTO<List<DocumentoVisorResponseDTO>> listarVisor(Integer idUsuario, String rol) {
        return documentoRepository.listarVisor(idUsuario, rol);
    }


    private String extraerPublicIdDeUrl(String url) {
        try {
            String folderPath = "sgac/documentos_academicos/";
            if (!url.contains(folderPath)) return null;

            int startIndex = url.indexOf(folderPath);
            int endIndex = url.lastIndexOf(".");

            return url.substring(startIndex, endIndex);
        } catch (Exception e) {
            log.error("No se pudo procesar la URL para extraer el publicId: {}", url);
            return null;
        }
    }
}