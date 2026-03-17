package org.uteq.sgacfinal.service.impl.documentos;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.repository.documentos.DocumentoAcademicoRepository;
import org.uteq.sgacfinal.service.cloudinary.CloudinaryUploadServiceImpl;
import org.uteq.sgacfinal.service.documentos.IDocumentoAcademicoService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentoAcademicoServiceImpl implements IDocumentoAcademicoService {

    private final DocumentoAcademicoRepository repo;
    private final CloudinaryUploadServiceImpl cloudinaryService;
    private final org.uteq.sgacfinal.service.security.SecurityContextService securitySvc;

    @Override
    @Transactional
    public DocumentoAcademico subirDocumento(MultipartFile file, String nombre, Integer idTipo, Integer idPeriodo, Integer idConvocatoria) {
        try {
            Map<String, Object> uploadResult = cloudinaryService.upload(file);
            DocumentoAcademico doc = new DocumentoAcademico();
            doc.setNombreMostrar(nombre);
            doc.setRutaArchivo(uploadResult.get("url").toString());
            doc.setExtension(uploadResult.get("extension") != null ? uploadResult.get("extension").toString() : "pdf");
            doc.setPesoBytes(((Long) uploadResult.get("pesoBytes")).intValue());
            doc.setFechaSubida(Instant.now());
            doc.setActivo(true);

            TipoDocumento td = new TipoDocumento();
            td.setId(idTipo);
            doc.setIdTipoDocumento(td);

            PeriodoAcademico pa = new PeriodoAcademico();
            pa.setIdPeriodoAcademico(idPeriodo);
            doc.setIdPeriodo(pa);

            if (idConvocatoria != null) {
                Convocatoria conv = new Convocatoria();
                conv.setIdConvocatoria(idConvocatoria);
                doc.setIdConvocatoria(conv);
            }

            Usuario user = new Usuario(); user.setIdUsuario(securitySvc.obtenerIdUsuario());
            doc.setIdUsuarioSube(user);

            return repo.save(doc);

        } catch (Exception e) {
            log.error("Error al procesar subida de documento: {}", nombre, e);
            throw new RuntimeException("Error al guardar el documento académico.");
        }
    }

    @Override
    public List<DocumentoAcademico> listarParaActores(Integer idPeriodo, Integer idConvocatoria) {
        return repo.buscarDocumentosPorContexto(idPeriodo, idConvocatoria);
    }
}