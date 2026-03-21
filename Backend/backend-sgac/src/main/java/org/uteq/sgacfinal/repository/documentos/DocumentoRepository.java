package org.uteq.sgacfinal.repository.documentos;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.request.documentos.*;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.documentos.*;
import org.uteq.sgacfinal.util.DatabaseService;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DocumentoRepository {
    private final DatabaseService db;

    public RespuestaOperacionDTO<List<FacultadResponseDTO>> getFacultades() {
        return db.ejecutarFuncion("academico", "fn_get_facultades_activas", null,
                new TypeReference<List<FacultadResponseDTO>>() {});
    }

    public RespuestaOperacionDTO<List<CarreraResponseDTO>> getCarreras(Integer idFacultad) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_facultad", idFacultad);

        return db.ejecutarFuncion("academico", "fn_get_carreras_activas", params,
                new TypeReference<List<CarreraResponseDTO>>() {});
    }

    public RespuestaOperacionDTO<List<TipoDocumentoResponseDTO>> getTiposDocumento() {
        return db.ejecutarFuncion("ayudantia", "fn_get_tipos_documento_activos", null,
                new TypeReference<List<TipoDocumentoResponseDTO>>() {});
    }

    public RespuestaOperacionDTO<DocumentoIdResponseDTO> insertar(DocumentoInsertRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_nombre_mostrar", req.nombreMostrar())
                .addValue("p_ruta_archivo", req.rutaArchivo())
                .addValue("p_extension", req.extension())
                .addValue("p_peso_bytes", req.pesoBytes())
                .addValue("p_id_tipo_doc", req.idTipoDoc())
                .addValue("p_id_usuario", req.idUsuario())
                .addValue("p_id_facultad", req.idFacultad())
                .addValue("p_id_carrera", req.idCarrera());

        return db.ejecutarFuncion("ayudantia", "fn_insertar_documento", params,
                new TypeReference<DocumentoIdResponseDTO>() {});
    }

    public RespuestaOperacionDTO<Void> actualizar(DocumentoUpdateRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_documento", req.idDocumento())
                .addValue("p_nombre_mostrar", req.nombreMostrar())
                .addValue("p_id_tipo_doc", req.idTipoDoc())
                .addValue("p_id_facultad", req.idFacultad())
                .addValue("p_id_carrera", req.idCarrera())
                .addValue("p_id_usuario", req.idUsuario());

        return db.ejecutarFuncion("ayudantia", "fn_actualizar_documento", params,
                new TypeReference<Void>() {});
    }

    public RespuestaOperacionDTO<DocumentoEliminadoResponseDTO> eliminar(Integer idDocumento) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_documento", idDocumento);

        return db.ejecutarFuncion("ayudantia", "fn_eliminar_documento", params,
                new TypeReference<DocumentoEliminadoResponseDTO>() {});
    }

    public RespuestaOperacionDTO<List<DocumentoVisorResponseDTO>> listarVisor(Integer idUsuario, String rol) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario)
                .addValue("p_nombre_rol", rol);

        return db.ejecutarFuncion("ayudantia", "fn_listar_documentos_visor", params,
                new TypeReference<List<DocumentoVisorResponseDTO>>() {});
    }
}