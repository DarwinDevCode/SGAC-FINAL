package org.uteq.sgacfinal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;
import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;

import java.sql.Types;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseService {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public <T> RespuestaOperacionDTO<T> ejecutarFuncion(
            String esquema,
            String funcion,
            MapSqlParameterSource params,
            TypeReference<T> typeRef) {

        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                    .withSchemaName(esquema)
                    .withFunctionName(funcion)
                    .withoutProcedureColumnMetaDataAccess();

            if (params != null)
                for (String paramName : params.getParameterNames())
                    jdbcCall.addDeclaredParameter(new SqlParameter(paramName, Types.OTHER));

            jdbcCall.addDeclaredParameter(new SqlOutParameter("valido", Types.BOOLEAN));
            jdbcCall.addDeclaredParameter(new SqlOutParameter("mensaje", Types.VARCHAR));
            jdbcCall.addDeclaredParameter(new SqlOutParameter("datos", Types.OTHER));

            Map<String, Object> out = (params != null) ? jdbcCall.execute(params) : jdbcCall.execute();

            boolean valido = parseBoolean(out.get("valido"));
            String mensaje = String.valueOf(out.getOrDefault("mensaje", ""));
            Object datosRaw = out.get("datos");

            T datos = null;
            if (datosRaw != null) {
                String jsonStr = (datosRaw instanceof String) ? (String) datosRaw : datosRaw.toString();
                if (!jsonStr.equals("null") && !jsonStr.isBlank())
                    datos = objectMapper.readValue(jsonStr, typeRef);

            }

            return new RespuestaOperacionDTO<>(valido, mensaje, datos);

        } catch (Exception e) {
            log.error("Error crítico en DatabaseService [ {}.{} ]: {}", esquema, funcion, e.getMessage());
            return new RespuestaOperacionDTO<>(false, "Error de infraestructura: " + e.getMessage(), null);
        }
    }

    private boolean parseBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).intValue() == 1;
        if (value instanceof String) return ((String) value).equalsIgnoreCase("true") || ((String) value).equalsIgnoreCase("t");
        return false;
    }
}