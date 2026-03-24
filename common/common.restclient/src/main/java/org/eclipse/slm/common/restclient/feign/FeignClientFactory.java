package org.eclipse.slm.common.restclient.feign;

import com.fasterxml.jackson.module.kotlin.KotlinModule;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.eclipse.slm.common.restclient.feign.auth.AuthRequestInterceptor;

import java.util.List;

public class FeignClientFactory {

    public static <T> T createClient(Class<T> clientClass, String baseUrl) {
        return createClient(clientClass, baseUrl, null);
    }

    public static <T> T createClient(Class<T> clientClass, String baseUrl, AuthRequestInterceptor authRequestInterceptor) {
        return createClient(clientClass, baseUrl, authRequestInterceptor, null);
    }

    public static <T> T createClient(Class<T> clientClass, String baseUrl, AuthRequestInterceptor authRequestInterceptor, ErrorDecoder customErrorDecoder) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl must not be null or empty");
        }

        var jacksonKotlinModule = new KotlinModule.Builder()
                .build();
        var decoder = new JacksonDecoder(List.of(jacksonKotlinModule));

        var apiClientBuilder = feign.Feign.builder()
                .decoder(decoder)
                .encoder(new JacksonEncoder());
        if (customErrorDecoder != null) {
            apiClientBuilder.errorDecoder(customErrorDecoder);
        }
        else {
            apiClientBuilder.errorDecoder(new ResponseErrorDecoder());
        }
        if (authRequestInterceptor != null) {
            apiClientBuilder.requestInterceptor(authRequestInterceptor);
        }

        var apiClient = apiClientBuilder.target(clientClass, baseUrl);

        return apiClient;
    }

}
