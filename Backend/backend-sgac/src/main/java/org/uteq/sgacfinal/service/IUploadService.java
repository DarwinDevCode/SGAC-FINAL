package org.uteq.sgacfinal.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IUploadService {
    Map<String, Object> upload(MultipartFile file);
}

