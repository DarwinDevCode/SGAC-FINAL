package org.uteq.sgacfinal.service.documentos;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.entity.DocumentoAcademico;

import java.util.List;

public interface IDocumentoAcademicoService {
    DocumentoAcademico subirDocumento(MultipartFile file, String nombre, Integer idTipo, Integer idPeriodo, Integer idConvocatoria);
    List<DocumentoAcademico> listarParaActores(Integer idPeriodo, Integer idConvocatoria);
}
