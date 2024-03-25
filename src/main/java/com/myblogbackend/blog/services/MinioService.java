package com.myblogbackend.blog.services;

import com.myblogbackend.blog.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioService {
    List<FileResponse> uploadMultiFiles(MultipartFile[] multipartFile);

}
