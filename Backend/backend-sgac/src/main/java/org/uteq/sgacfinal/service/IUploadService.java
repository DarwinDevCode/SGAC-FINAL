package org.uteq.sgacfinal.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IUploadService {
    Map<String, Object> upload(byte[] fileBytes, String originalName, String mimeType);
    Map<String, Object> upload(MultipartFile file);
}

