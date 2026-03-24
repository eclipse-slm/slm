package org.eclipse.slm.common.minio.model.exceptions;

public class MinioBucketCreateException extends Exception{

    public MinioBucketCreateException(String bucketName) {
        super("Bucket with name \"" + bucketName + "\" could not be created");
    }
}
