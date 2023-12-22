package org.eclipse.slm.common.minio.client;

import io.minio.*;
import io.minio.errors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MinioClient {

    private static final Logger log = LoggerFactory.getLogger(MinioClient.class);


    private final String url;

    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;


    private io.minio.MinioClient minioClient;

    public MinioClient(
            @Value("${minio.scheme}") String scheme,
            @Value("${minio.host}") String host,
            @Value("${minio.port}") int port
    ) {
        this.url = scheme + "://" + host + ":" + port;
    }

    @PostConstruct
    public void init() {
        this.minioClient = io.minio.MinioClient.builder()
                .endpoint(this.url)
                .credentials(this.accessKey, this.secretKey)
                .build();
    }

    public boolean bucketExist(String name) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
        } catch (MinioException e) {
            log.error("Error occurred: " + e);
            return false;
        }
    }

    public void createBucket(String name) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
        } catch (MinioException e) {
            log.error("Error occurred: " + e);
        }
    }

    public void uploadObject(String bucketName, String objectName, File file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            var hasBucket = this.bucketExist(bucketName);
            if (!hasBucket) {
                this.createBucket(bucketName);
            }

            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(file.getAbsolutePath())
                    .build());
        } catch (MinioException e) {
            log.error("Error occurred: " + e);
        }
    }

    public boolean objectExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
            return true;
        } catch (ErrorResponseException e) {
            log.error("Not found", e);
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getObjectContentAsString(String bucketName, String objectName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (ErrorResponseException e) {
            log.error("Not found", e);
            return "";
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

}
