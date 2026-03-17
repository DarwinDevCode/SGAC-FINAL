package org.uteq.sgacfinal.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryUploadServiceImpl {
    private final Cloudinary cloudinary;
    private static final String FOLDER = "sgac/documentos_academicos";

    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio.");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "archivo";
        String baseName = originalName.contains(".")
                ? originalName.substring(0, originalName.lastIndexOf("."))
                : originalName;
        String publicId = baseName.replaceAll("[^a-zA-Z0-0]", "_") + "-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", FOLDER,
                            "public_id", publicId,
                            "resource_type", "auto",
                            "access_mode", "public"
                    )
            );

            String url = result.get("secure_url") != null
                    ? result.get("secure_url").toString()
                    : result.get("url").toString();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("url", url);
            response.put("publicId", result.get("public_id"));
            response.put("resourceType", result.get("resource_type"));
            response.put("extension", result.get("format"));
            response.put("pesoBytes", file.getSize());
            response.put("mimeType", file.getContentType());

            return response;

        } catch (IOException ex) {
            log.error("Error subiendo archivo a Cloudinary: {}", originalName, ex);
            throw new RuntimeException("No se pudo subir el archivo: " + originalName);
        }
    }

    public void delete(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}