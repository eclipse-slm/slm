package org.eclipse.slm.common.minio.model.exceptions;

public class MinioRemoveObjectException extends Exception{
    public MinioRemoveObjectException(String objectName) {
        super("Object with name \"" + objectName + "\" could not be removed");
    }
}
