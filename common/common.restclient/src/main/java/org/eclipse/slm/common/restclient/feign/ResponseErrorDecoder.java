package org.eclipse.slm.common.restclient.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ResponseErrorDecoder implements ErrorDecoder {

    private final Logger LOG = LoggerFactory.getLogger(ResponseErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        String body = null;
        try {
            if (response.body() != null) {
                body = new String(response.body().asInputStream().readAllBytes());
            }
        } catch (IOException e) {
            body = "Error reading response body: " + e.getMessage();
        }

        return new FeignResponseException(methodKey, response, body);
    }
}

