package org.uteq.sgacfinal.service.impl.configuracion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseBackupService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private final String BACKUP_FOLDER = "C:/SGAC-FINAL/backups/";

    private final String ADMIN_USER = "admin1";
    private final String ADMIN_PASS = "admin1";

    public RespuestaOperacionDTO<String> generarRespaldo() {
        try {
            File folder = new File(BACKUP_FOLDER);
            if (!folder.exists()) folder.mkdirs();

            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String rutaDestino = BACKUP_FOLDER + "sgac_backup_" + fecha + ".dump";
            String dbName = extraeNombreBd(dbUrl);

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "pg_dump", "-h", "localhost", "-p", "5432", "-U", ADMIN_USER,
                    "-F", "c", "-f", rutaDestino, dbName
            );

            processBuilder.environment().put("PGPASSWORD", ADMIN_PASS);
            processBuilder.inheritIO();

            Process process = processBuilder.start();
            int processComplete = process.waitFor();
            eliminarRespaldosAntiguos();

            if (processComplete == 0) {
                log.info("Backup creado con éxito en: {}", rutaDestino);
                return new RespuestaOperacionDTO<>(true, "Respaldo generado correctamente", rutaDestino);
            } else
                return new RespuestaOperacionDTO<>(false, "Fallo al ejecutar pg_dump", null);


        } catch (Exception e) {
            log.error("Error crítico al generar backup", e);
            return new RespuestaOperacionDTO<>(false, "Error: " + e.getMessage(), null);
        }
    }

    public RespuestaOperacionDTO<Void> restaurarRespaldo(String nombreArchivo) {
        try {
            String rutaArchivo = BACKUP_FOLDER + nombreArchivo;
            File backupFile = new File(rutaArchivo);

            if (!backupFile.exists())
                return new RespuestaOperacionDTO<>(false, "El archivo no existe.", null);

            String dbName = extraeNombreBd(dbUrl);

            try {
                jdbcTemplate.execute("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '" + dbName + "' AND pid <> pg_backend_pid();");
                log.info("Conexiones activas terminadas.");
            } catch (Exception e) {
                log.warn("No se pudieron cerrar otras conexiones, continuando...");
            }

            ProcessBuilder processBuilder = new ProcessBuilder(
                    "pg_restore", "-h", "localhost", "-p", "5432", "-U", ADMIN_USER,
                    "-d", dbName,
                    "--clean",
                    "--if-exists",
                    "-O",
                    "-x",
                    rutaArchivo
            );

            processBuilder.environment().put("PGPASSWORD", ADMIN_PASS);
            processBuilder.inheritIO();

            Process process = processBuilder.start();
            int processComplete = process.waitFor();

            if (processComplete == 0 || processComplete == 1) {
                log.info("Restauración completada desde: {}", rutaArchivo);
                return new RespuestaOperacionDTO<>(true, "Base de datos restaurada correctamente", null);
            } else
                return new RespuestaOperacionDTO<>(false, "Fallo al ejecutar pg_restore", null);
        } catch (Exception e) {
            log.error("Error al restaurar backup", e);
            return new RespuestaOperacionDTO<>(false, "Error: " + e.getMessage(), null);
        }
    }

    public List<String> listarRespaldos() {
        File folder = new File(BACKUP_FOLDER);
        if (!folder.exists()) return new ArrayList<>();

        File[] listOfFiles = folder.listFiles((dir, name) -> name.endsWith(".dump"));
        if (listOfFiles == null) return new ArrayList<>();

        return Arrays.stream(listOfFiles)
                .map(File::getName)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private String extraeNombreBd(String url) {
        return url.substring(url.lastIndexOf("/") + 1).split("\\?")[0];
    }

    private void eliminarRespaldosAntiguos() {
        log.info("Iniciando limpieza de respaldos antiguos (Retención: 4 meses)...");
        try {
            File folder = new File(BACKUP_FOLDER);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".dump"));

            if (files == null || files.length == 0) return;

            long limiteMilisegundos = System.currentTimeMillis() - (120L * 24 * 60 * 60 * 1000);

            int eliminados = 0;
            for (File file : files)
                if (file.lastModified() < limiteMilisegundos)
                    if (file.delete()) {
                        log.info("Archivo eliminado por antigüedad: {}", file.getName());
                        eliminados++;
                    }

            log.info("Limpieza terminada. Se eliminaron {} archivos.", eliminados);
        } catch (Exception e) {
            log.error("Error al limpiar archivos antiguos", e);
        }
    }
}