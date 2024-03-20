package org.eclipse.slm.common.minio.model.exceptions;

import java.text.MessageFormat;
import java.util.List;

public class MinioBucketNameException extends Exception {

    private final List<String> errors;

    public MinioBucketNameException(List<String> errors) {
        super(String.join(" ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
