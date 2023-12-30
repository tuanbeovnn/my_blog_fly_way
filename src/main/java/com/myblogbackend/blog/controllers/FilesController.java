package com.myblogbackend.blog.controllers;

import com.myblogbackend.blog.controllers.route.CommonRoutes;
import com.myblogbackend.blog.services.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(CommonRoutes.BASE_API + CommonRoutes.VERSION + "/files")
@RequiredArgsConstructor
public class FilesController {

//    private final MinioService minioService;
//
//    @PostMapping("/upload")
//    public ResponseEntity<?> registerUser(final @ModelAttribute MultipartFile file) {
//        var upload = minioService.uploadFile(file);
//        return ResponseEntity.ok(upload);
//    }
}
