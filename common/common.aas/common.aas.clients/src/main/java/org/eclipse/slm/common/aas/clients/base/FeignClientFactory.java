package org.eclipse.slm.common.aas.clients.base;

import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;

import java.io.IOException;

public class FeignClientFactory {

    public static <T> T createClient(Class<T> clientClass, String baseUrl, RequestInterceptor requestInterceptor) {
        Decoder decoder = (response, type) -> {
            try {
                return new JsonDeserializer().read(response.body().asInputStream(), (Class<?>) type);
            } catch (IOException | DeserializationException e) {
                throw new RuntimeException(e);
            }
        };

        Encoder encoder = (object, bodyType, template) -> {
            try {
                var jsonSerializer = new JsonSerializer();
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
        if (requestInterceptor != null) {
            apiClientBuilder.requestInterceptor(requestInterceptor);
        }

        var apiClient = apiClientBuilder.target(clientClass, baseUrl);

        return apiClient;
    }

}
