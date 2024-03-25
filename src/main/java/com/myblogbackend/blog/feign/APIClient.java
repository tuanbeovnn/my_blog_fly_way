package com.myblogbackend.blog.feign;

import com.myblogbackend.blog.response.FileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(value = "uploadFiles", url = "http://localhost:8081", configuration = FeignSupportConfig.class)
public interface APIClient {
    @PostMapping(value = "/minio/upload/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<FileResponse> uploadMultipleFiles(final MultipartFile[] files);

}