package org.eclipse.slm.common.minio.client;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.eclipse.slm.common.minio.model.BucketName;
import org.eclipse.slm.common.minio.model.ObjectName;
import org.eclipse.slm.common.minio.model.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MinioClient {

    private static final Logger LOG = LoggerFactory.getLogger(MinioClient.class);

    private final String minioUrl;
    private final String accessKey;
    private final String secretKey;

    private io.minio.MinioClient minioClient;

    public MinioClient(
            @Value("${minio.scheme}") String scheme,
            @Value("${minio.host}") String host,
            @Value("${minio.port}") int port,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey
    ) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.minioUrl = scheme + "://" + host + ":" + port;
    }

    @PostConstruct
    public void init() {
        this.minioClient = io.minio.MinioClient.builder()
                .endpoint(this.minioUrl)
                .credentials(this.accessKey, this.secretKey)
                .build();
    }

    public boolean bucketExist(String name) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            LOG.debug("Bucket with name '" + name + "' not found: ", e);
            return false;
        }
    }

    public void createBucket(String name) throws MinioBucketCreateException, MinioBucketNameException {
        createBucket(BucketName.withName(name));
    }

    public void createBucket(BucketName bucketName) throws MinioBucketCreateException {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName.getName()).build());
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            LOG.error("Bucket with name '" + bucketName + "' could not be created: ", e);
            throw new MinioBucketCreateException(bucketName.getName());
        }
    }

    public void uploadObject(BucketName bucketName, ObjectName objectName, File file) throws MinioBucketCreateException, MinioUploadException {
        try {
            var hasBucket = this.bucketExist(bucketName.getName());
            if (!hasBucket) {
                this.createBucket(bucketName);
            }

            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucketName.getName())
                    .object(objectName.getName())
                    .filename(file.getAbsolutePath())
                    .build());
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOG.debug("Object with name '" + objectName + "' could not be uploaded to bucket '" + bucketName + "': ", e);
            throw new MinioUploadException(bucketName.getName(), objectName.getName());
        }
    }

    public void uploadObject(String bucketName, String objectName, File file) throws MinioBucketCreateException, MinioUploadException, MinioBucketNameException, MinioObjectPathNameException {
        uploadObject(BucketName.withName(bucketName), ObjectName.withName(objectName), file);
    }

    public void putObject(BucketName bucketName, ObjectName objectName, InputStream stream, long size, String contentType) throws MinioBucketCreateException, MinioUploadException {
        try {
            var hasBucket = this.bucketExist(bucketName.getName());
            if (!hasBucket) {
                this.createBucket(bucketName);
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName.getName())
                    .object(objectName.getName())
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            LOG.error("Object with name '" + objectName + "' could not be uploaded to bucket '" + bucketName + "' ",e);
            throw new MinioUploadException(bucketName.getName(), objectName.getName());
        }
    }

    public void putObject(String bucketName, String objectName, InputStream stream, long size, String contentType) throws MinioBucketCreateException, MinioUploadException, MinioBucketNameException, MinioObjectPathNameException {
        putObject(BucketName.withName(bucketName), ObjectName.withName(objectName), stream, size, contentType);
    }

    public boolean objectExist(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build());
            return true;
        } catch (Exception e) {
            LOG.debug("Object with name '" + objectName + "' not found in bucket '" + bucketName + "': ", e);
            return false;
        }
    }

    public String getObjectContentAsString(String bucketName, String objectName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Object with name '" + objectName + "' not found in bucket '" + bucketName + "': ", e);
            return "";
        }
    }

    public void removeObject(BucketName bucketName, ObjectName objectName) throws MinioRemoveObjectException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName.getName())
                    .object(objectName.getName())
                    .build()
            );
        } catch (Exception e) {
            LOG.error("Object with name '" + objectName + "' could not be removed from bucket '" + bucketName + "': ", e);
            throw new MinioRemoveObjectException(objectName.getName());
        }
    }

    public void removeObject(String bucketName, String objectName) throws MinioRemoveObjectException, MinioObjectPathNameException, MinioBucketNameException {
        removeObject(BucketName.withName(bucketName), ObjectName.withName(objectName));
    }

    public InputStream getObject(BucketName bucketName, ObjectName objectName) throws Exception {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName.getName())
                    .object(objectName.getName())
                    .build()
            );
        } catch (Exception e) {
            LOG.error("Object with name '" + objectName + "' could not be removed from bucket '" + bucketName + "': ", e);
            throw new Exception(objectName.getName());
        }
    }

    public InputStream getObject(String bucketName, String objectName) throws Exception {
        return getObject(BucketName.withName(bucketName), ObjectName.withName(objectName));
    }

    public List<Item> getObjectsOfPath(String bucketName, String objectsPath) {
        var listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(objectsPath)
                .recursive(true)
                .build();
        var listObjectsResult = minioClient.listObjects(listObjectsArgs);

        var items = new ArrayList<Item>();
        for (Result<Item> itemResult : listObjectsResult) {
            try {
                var item = itemResult.get();
                items.add(item);
                LOG.debug("Found object: " + item.objectName() + " in bucket: " + bucketName);
            } catch (Exception e) {
                LOG.error("Error while listing objects in bucket '" + bucketName + "': ", e);
            }
        }

        return items;
    }

}
