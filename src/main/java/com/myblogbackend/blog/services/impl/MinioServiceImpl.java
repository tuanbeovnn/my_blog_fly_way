package com.myblogbackend.blog.services.impl;

import com.myblogbackend.blog.config.minio.FileMetadata;
import com.myblogbackend.blog.config.minio.MinioProperties;
import com.myblogbackend.blog.services.MinioService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public FileMetadata uploadFile(final MultipartFile multipartFile) {
        //check if mimeType includes (image) -> throw exception
        String fileKey = createKey(multipartFile);
        return put(minioProperties.getBucketName(), fileKey, multipartFile, true);
    }

    private String createKey(final MultipartFile file) {
        String tagFile = file.getOriginalFilename();
        if (tagFile == null) {
            throw new RuntimeException("File is empty");
        }
        String splitTag = tagFile.substring(tagFile.lastIndexOf("."));
        int tagFileSub = tagFile.lastIndexOf(".");
        String name = tagFile.substring(0, Math.min(tagFileSub, 10));
        return name + UUID.randomUUID() + splitTag;
    }

    public FileMetadata put(final String bucket, final String key, final MultipartFile file, final Boolean publicAccess) {
        var metadata = FileMetadata.builder()
                .bucket(bucket)
                .key(key)
                .name(file.getOriginalFilename())
                .extension(StringUtils.getFilenameExtension(file.getOriginalFilename()))
                .mime(detectMimeType(file))
                .size(file.getSize())
                .build();


        try {
            var upload = UploadObjectArgs
                    .builder()
                    .bucket(minioProperties.getBucketName())
                    .filename(metadata.getKey())
                    .contentType(metadata.getMime())
                    .build();
            minioClient.uploadObject(upload);
        } catch (IOException e) {
            throw new RuntimeException("IOException");
        } catch (ServerException | InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException |
                 InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new RuntimeException(e);
        }
        return metadata;
    }

    public String detectMimeType(final MultipartFile multipartFile) {
        Detector detector = new DefaultDetector();
        Metadata metadata = new Metadata();
        try (InputStream stream = multipartFile.getInputStream()) {
            MediaType mediaType = detector.detect(stream, metadata);
            return mediaType.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
