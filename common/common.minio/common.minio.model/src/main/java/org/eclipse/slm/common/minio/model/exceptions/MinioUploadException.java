package org.eclipse.slm.common.minio.model.exceptions;

public class MinioUploadException extends Exception{
    public MinioUploadException(String bucketName, String objectName) {
        super("Object with name \"" + objectName + "\" could not uploaded to \"" + bucketName + "\"");
    }
}
