package com.myblogbackend.blog.feign;

import com.myblogbackend.blog.response.FileResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "people-api", url = "${file.upload.url}")
public interface OutboundUploadMultiFiles {

    Logger logger = LogManager.getLogger(OutboundUploadMultiFiles.class);

    @PostMapping(value = "/minio/upload/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Retry(name = "peopleProxyRetry")
    @CircuitBreaker(name = "peopleProxyCircuitBreaker", fallbackMethod = "serviceFallbackMethod")
    List<FileResponse> uploadMultipleFiles(@RequestPart("files") final MultipartFile[] files);

    default List<FileResponse> serviceFallbackMethod(Throwable exception) {
        logger.error(
                "Data server is either unavailable or malfunctioned due to {}", exception.getMessage());
        throw new RuntimeException(exception.getMessage());
    }

}