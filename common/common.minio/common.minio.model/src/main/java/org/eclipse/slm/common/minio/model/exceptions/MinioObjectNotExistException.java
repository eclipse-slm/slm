package org.eclipse.slm.common.minio.model.exceptions;

public class MinioObjectNotExistException extends Exception {
    public MinioObjectNotExistException(String objectName) {
        super("Object with name \"" + objectName + "\" not found");
    }
}
