package org.eclipse.slm.common.minio.client;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MinioClient {

    private final String url;

    @Value("${minio.access-key}")
    private String accessKey;
    @Value("${minio.secret-key}")
    private String secretKey;


    private io.minio.MinioClient minioClient;

    public MinioClient(
            @Value("${minio.scheme}") String scheme,
            @Value("${minio.host}")String host,
            @Value("${minio.port}")int port
    ) {
        this.url = scheme + "://" + host + ":" + port;
    }

    @PostConstruct
    public void init(){
        this.minioClient = io.minio.MinioClient.builder()
                .endpoint(this.url)
                .credentials(this.accessKey, this.secretKey)
                .build();
    }

    public boolean existBucket(String name) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            return false;
        }
    }

    public void createBucket(String name) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }
    }

    public void uploadObject(String bucketName, String objectName, File file) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(file.getAbsolutePath())
                    .build());
        }catch (MinioException e) {
            System.out.println("Error occurred: " + e);
        }
    }

}
