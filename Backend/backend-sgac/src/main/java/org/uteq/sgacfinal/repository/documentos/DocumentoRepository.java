package org.uteq.sgacfinal.repository.documentos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.Request.documentos.*;
import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.Response.documentos.*;

import java.util.List;
import java.util.Map;

@Repository
public class DocumentoRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DocumentoRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public RespuestaOperacionDTO<List<FacultadResponseDTO>> getFacultades() {
        return ejecutarConsulta("fn_get_facultades_activas", "academico", null,
                new TypeReference<List<FacultadResponseDTO>>() {});
    }

    public RespuestaOperacionDTO<List<CarreraResponseDTO>> getCarreras(Integer idFacultad) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_facultad", idFacultad);
        return ejecutarConsulta("fn_get_carreras_activas", "academico", params,
                new TypeReference<List<CarreraResponseDTO>>() {});
    }

    public RespuestaOperacionDTO<List<TipoDocumentoResponseDTO>> getTiposDocumento() {
        return ejecutarConsulta("fn_get_tipos_documento_activos", "ayudantia", null,
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

        return ejecutarConsulta("fn_insertar_documento", "ayudantia", params,
                new TypeReference<DocumentoIdResponseDTO>() {});
    }

    public RespuestaOperacionDTO<Void> actualizar(DocumentoUpdateRequestDTO req) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_documento", req.idDocumento())
                .addValue("p_nombre_mostrar", req.nombreMostrar())
                .addValue("p_id_tipo_doc", req.idTipoDoc())
                .addValue("p_id_facultad", req.idFacultad())
                .addValue("p_id_carrera", req.idCarrera());

        return ejecutarConsulta("fn_actualizar_documento", "ayudantia", params,
                new TypeReference<Void>() {});
    }

    public RespuestaOperacionDTO<DocumentoEliminadoResponseDTO> eliminar(Integer idDocumento) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_documento", idDocumento);

        return ejecutarConsulta("fn_eliminar_documento", "ayudantia", params,
                new TypeReference<DocumentoEliminadoResponseDTO>() {});
    }

    public RespuestaOperacionDTO<List<DocumentoVisorResponseDTO>> listarVisor(Integer idUsuario, String rol) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_id_usuario", idUsuario)
                .addValue("p_nombre_rol", rol);

        return ejecutarConsulta("fn_listar_documentos_visor", "ayudantia", params,
                new TypeReference<List<DocumentoVisorResponseDTO>>() {});
    }

    private <T> RespuestaOperacionDTO<T> ejecutarConsulta(String funcion, String esquema, MapSqlParameterSource params, TypeReference<T> typeRef) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withSchemaName(esquema)
                    .withFunctionName(funcion);

            Map<String, Object> out = (params != null) ? jdbcCall.execute(params) : jdbcCall.execute();

            boolean valido = (boolean) out.get("valido");
            String mensaje = (String) out.get("mensaje");

            T datos = objectMapper.convertValue(out.get("datos"), typeRef);

            return new RespuestaOperacionDTO<>(valido, mensaje, datos);
        } catch (Exception e) {
            return new RespuestaOperacionDTO<>(false, "Error de conexión/mapeo: " + e.getMessage(), null);
        }
    }
}