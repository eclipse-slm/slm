package org.eclipse.slm.common.minio.model.exceptions;

import java.util.List;

public class MinioObjectPathNameException extends Exception {

    private final List<String> errors;

    public MinioObjectPathNameException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
