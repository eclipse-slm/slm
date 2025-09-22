package org.eclipse.slm.common.utils.general;

import java.util.Base64;

public class Base64Util {

    public static String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static String decodeFromBase64(String base64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);
        return new String(decodedBytes);
    }

}
