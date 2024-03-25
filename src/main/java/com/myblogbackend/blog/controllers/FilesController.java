package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.response.FileResponse;
import com.myblogbackend.blog.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + "/files")
@RequiredArgsConstructor
public class FilesController {
    private static final Logger LOGGER = LogManager.getLogger(FilesController.class);

    private final MinioService minioService;

    @PostMapping("/upload/client/files")
    public List<FileResponse> uploadFiles(final MultipartFile[] files) {
        return minioService.uploadMultiFiles(files);
    }
}
