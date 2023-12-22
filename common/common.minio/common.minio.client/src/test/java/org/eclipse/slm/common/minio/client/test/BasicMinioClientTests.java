package org.eclipse.slm.common.minio.client.test;

import org.eclipse.slm.common.minio.client.MinioClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        MinioClient.class
})
@ContextConfiguration(initializers = {ConfigDataApplicationContextInitializer.class})
@TestPropertySource(properties = {"spring.config.location=classpath:application.yml"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicMinioClientTests {

    //region Variables
    public final static Logger LOG = LoggerFactory.getLogger(BasicMinioClientTests.class);
    public final static GenericContainer<?> minioDockerContainer;
    private static int MINIO_PORT = 9000;
    private static int MINIO_API_PORT = 9090;

    @Autowired
    private MinioClient minioClient;

    @DynamicPropertySource
    static void consulProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.port", minioDockerContainer::getFirstMappedPort);
    }

    static {
        minioDockerContainer = new GenericContainer<>(DockerImageName.parse("quay.io/minio/minio:RELEASE.2023-12-20T01-00-02Z"))
                .withEnv("MINIO_ACCESS_KEY", "admin")
                .withEnv("MINIO_SECRET_KEY", "password")
                .withCommand("server /data")
                .withExposedPorts(MINIO_PORT)
                .waitingFor(new HttpWaitStrategy()
                        .forPath("/minio/health/ready")
                        .forPort(MINIO_PORT)
                        .withStartupTimeout(Duration.ofSeconds(10)));
        minioDockerContainer.start();
    }

    @AfterAll
    public static void afterAll() {
        minioDockerContainer.stop();
    }

    @BeforeTestExecution()
    public void beforeTestExecution() {
        assertNotEquals(null, this.minioClient);
    }

    @Test
    public void shouldCreateANewBucket() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        var bucketName = "should-create-a-new-bucket";
        minioClient.createBucket(bucketName);
        assertTrue(minioClient.bucketExist(bucketName));
    }

    @Test
    public void shouldUploadFileToNotExistedBucketAndCheckIfExist() throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        var bucketName = "upload-file-not-existed-bucket";
        var objectName = "/test/Object";
        var file = new File("src/test/resources/test.txt");

        minioClient.uploadObject(bucketName, objectName, file);

        assertTrue(minioClient.bucketExist(bucketName));
        assertTrue(minioClient.objectExist(bucketName, objectName));
    }

    @Test
    public void shouldUploadFileToBucketAndCheckIfExist() throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        var bucketName = "upload-file-existed-bucket";
        var objectName = "/test/Object";
        var file = new File("src/test/resources/test.txt");

        minioClient.createBucket(bucketName);
        minioClient.uploadObject(bucketName, objectName, file);

        assertTrue(minioClient.bucketExist(bucketName));
        assertTrue(minioClient.objectExist(bucketName, objectName));
    }

    @Test
    public void shouldGetContentOfUploadedObject() throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        var bucketName = "get-content-of-uploaded-object";
        var objectName = "/test/Object";
        var file = new File("src/test/resources/test.txt");
        var fileContent = Files.readString(file.toPath());

        minioClient.uploadObject(bucketName, objectName, file);

        var objectContent = minioClient.getObjectContentAsString(bucketName, objectName);

        assertEquals(fileContent, objectContent);
    }


}
