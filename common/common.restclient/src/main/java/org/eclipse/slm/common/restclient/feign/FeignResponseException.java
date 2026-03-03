package org.eclipse.slm.common.restclient.feign;

import feign.Response;

import java.util.Collection;
import java.util.Map;

public class FeignResponseException extends RuntimeException {

    private int statusCode;

    private String requestUrl;

    private String requestHttpMethod;

    private Map<String, Collection<String>> requestHeaders;

    private String body;


    public FeignResponseException(String methodKey, Response response, String body) {
        super(String.format("Error in method %s: Status %d, Body: %s, Request: %s", methodKey, response.status(), body, response.request()));
        this.statusCode = response.status();
        this.requestUrl = response.request().url();
        this.requestHttpMethod = response.request().httpMethod().toString();
        this.requestHeaders = response.request().headers();
        this.body = body;

    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getRequestHttpMethod() {
        return requestHttpMethod;
    }

    public Map<String, Collection<String>> getRequestHeaders() {
        return requestHeaders;
    }
}
