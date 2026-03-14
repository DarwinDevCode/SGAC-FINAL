package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.uteq.sgacfinal.service.IUploadService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryUploadServiceImpl implements IUploadService {

    @Value("${app.file.upload-dir}")
    private String baseUploadDir;

    @Override
    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio.");
        }
        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo";
            return this.upload(file.getBytes(), originalName, file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Error al leer los bytes del archivo", e);
        }
    }

    @Override
    public Map<String, Object> upload(byte[] fileBytes, String originalName, String mimeType) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("El archivo es obligatorio.");
        }

        String actualName = originalName != null ? originalName : "archivo";
        String baseName = actualName.replaceAll("\\.[^\\.]+$", "");
        String publicId = baseName + "-" + UUID.randomUUID() + ".pdf";

        try {
            Path uploadPath = Paths.get(baseUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(publicId);
            Files.write(filePath, fileBytes);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/")
                    .path(publicId)
                    .toUriString();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("url", url);
            response.put("resourceType", "raw");
            response.put("tamanioBytes", fileBytes.length);
            response.put("mimeType", mimeType != null ? mimeType : "application/pdf");
            return response;

        } catch (Exception ex) {
            throw new RuntimeException("Error al guardar archivo localmente: " + actualName, ex);
        }
    }
}
