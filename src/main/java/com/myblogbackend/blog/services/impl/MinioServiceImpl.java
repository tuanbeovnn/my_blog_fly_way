package com.myblogbackend.blog.services.impl;


import com.myblogbackend.blog.feign.OutboundUploadMultiFiles;
import com.myblogbackend.blog.response.FileResponse;
import com.myblogbackend.blog.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final static Logger LOGGER = LogManager.getLogger(MinioServiceImpl.class);

    private final OutboundUploadMultiFiles outboundUploadMultiFiles;

    @Override
    public List<FileResponse> uploadMultiFiles(final MultipartFile[] multipartFile) {
        try {
            return outboundUploadMultiFiles.uploadMultipleFiles(multipartFile);
        } catch (Exception e) {
            LOGGER.error("Error occurred while uploading files", e);
            throw new RuntimeException("Failed to upload files", e);
        }
    }
}
