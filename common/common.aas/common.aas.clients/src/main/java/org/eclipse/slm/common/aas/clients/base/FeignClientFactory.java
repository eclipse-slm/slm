package org.eclipse.slm.common.aas.clients.base;

import feign.codec.Decoder;
import feign.codec.Encoder;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.slm.common.aas.clients.auth.AuthRequestInterceptor;

import java.io.IOException;

public class FeignClientFactory {

    public static <T> T createClient(Class<T> clientClass, String baseUrl, AuthRequestInterceptor authRequestInterceptor) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or empty");
        }

        Decoder decoder = (response, type) -> {
            String body = "";
            try {
                body = new String(response.body().asInputStream().readAllBytes());
                return new JsonDeserializer().read(body, (Class<?>) type);
            } catch (IOException | DeserializationException e) {
                if (e instanceof DeserializationException) {
                    throw new RuntimeException("DeserializationException: " + e.getMessage() + ", Response Body: " + body, e);
                }
                throw new RuntimeException();
            }
        };

        Encoder encoder = (object, bodyType, template) -> {
            try {
                var jsonSerializer = new CustomAasJsonSerializer();
                var json = jsonSerializer.write(object);
                template.body(json);
            } catch (org.eclipse.digitaltwin.aas4j.v3.dataformat.core.SerializationException e) {
                throw new RuntimeException(e);
            }
        };

        var apiClientBuilder = feign.Feign.builder()
                .decoder(decoder)
                .encoder(encoder)
                .errorDecoder(new ResponseErrorDecoder());
        if (authRequestInterceptor != null) {
            apiClientBuilder.requestInterceptor(authRequestInterceptor);
        }

        var apiClient = apiClientBuilder.target(clientClass, baseUrl);

        return apiClient;
    }

}
