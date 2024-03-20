package org.eclipse.slm.common.minio.model;

import org.eclipse.slm.common.minio.model.exceptions.MinioBucketNameException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BucketName {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 63;
    private static final Pattern VALID_CHARACTERS = Pattern.compile("^[a-z0-9.-]+$");

    private final String name;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    private BucketName(String name) {
        this.name = name;
    }

    public static BucketName withName(String name) throws MinioBucketNameException {
        var errors = isValidBucketName(name);
        if (!errors.isEmpty()) {
            throw new MinioBucketNameException(errors);
        }
        return new BucketName(name);
    }

    private static List<String> isValidBucketName(String bucketName) {
        List<String> errors = new ArrayList<>();

        int length = bucketName.length();
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            errors.add("Bucket name length should be between 3 and 63 characters.");
        }

        if (!VALID_CHARACTERS.matcher(bucketName).matches()) {
            errors.add("Bucket name can only contain lowercase letters, numbers, periods, and dashes.");
        }

        if (bucketName.startsWith("-") || bucketName.endsWith("-")) {
            errors.add("Bucket name should not start or end with a dash.");
        }

        if (bucketName.contains("-.")) {
            errors.add("Bucket name should not contain dashes next to periods.");
        }

        return errors;
    }


}
