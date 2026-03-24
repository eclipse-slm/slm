package org.eclipse.slm.common.minio.model.exceptions;

public class MinioBucketNotExistException extends Exception{
    public MinioBucketNotExistException(String bucketName) {
        super("Bucket with name \"" + bucketName + "\" not found");
    }
}
