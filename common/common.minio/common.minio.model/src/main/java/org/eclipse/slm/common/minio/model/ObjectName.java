package org.eclipse.slm.common.minio.model;

import org.eclipse.slm.common.minio.model.exceptions.MinioObjectPathNameException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ObjectName {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 1024;
    private static final Pattern VALID_CHARACTERS = Pattern.compile("^[a-zA-Z0-9!\\\\\"&'()+,\\-._\\/;=@*]+$");

    private final String objectName;

    public String getName() {
        return objectName;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    private ObjectName(String objectName) {
        this.objectName = objectName;
    }

    public static ObjectName withName(String name) throws MinioObjectPathNameException {
        var errors = checkObjectName(name);
        if (!errors.isEmpty()) {
            throw new MinioObjectPathNameException(errors);
        }
        return new ObjectName(name);
    }

    private static List<String> checkObjectName(String objectName) {
        List<String> errors = new ArrayList<>();

        int length = objectName.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            errors.add("Object name length should be between 1 and 1024 characters.");
        }

        if (!VALID_CHARACTERS.matcher(objectName).matches()) {
            errors.add("Object name contains invalid characters.");
        }

        return errors;
    }


}
