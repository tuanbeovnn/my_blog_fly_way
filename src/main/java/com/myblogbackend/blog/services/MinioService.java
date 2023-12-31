package com.myblogbackend.blog.services;

import com.myblogbackend.blog.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MinioService {

    //Check Whether bucket already exists
    boolean bucketExists(String bucketName);

    // Create a bucket
    void makeBucket(String bucketName);

    // List all bucket names
//    List<String> listBucketName();

    // List all object names in the bucket
//    List<String> listObjectNames(String bucketName);

    // Upload files in the bucket
    FileResponse putObject(MultipartFile multipartFile, String bucketName, String fileType);

    // Upload list file in the bucket
    List<FileResponse> putObjects(MultipartFile[] multipartFile, String bucketName, String fileType);

}
