package org.eclipse.slm.service_management.model.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Base64;

public class ByteArrayDeserializer extends JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonParser jsonparser, DeserializationContext arg1)
            throws IOException, JsonProcessingException {

        String data = jsonparser.getText();

        try {

            var splitString = data.split("base64,");
            var base64String = "";
            if (splitString.length > 1)
            {
                base64String = splitString[1];
            }
            else
            {
                base64String = data;
            }

            byte[] decodedBytes = Base64.getDecoder().decode(base64String.getBytes("UTF-8"));
            return decodedBytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
