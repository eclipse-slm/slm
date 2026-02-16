package org.eclipse.slm.common.vault.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.eclipse.slm.common.restclient.feign.FeignResponseException;
import org.eclipse.slm.common.restclient.feign.ResponseErrorDecoder;
import org.eclipse.slm.common.vault.client.exceptions.VaultPermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VaultResponseErrorDecoder implements ErrorDecoder {

    private final Logger LOG = LoggerFactory.getLogger(VaultResponseErrorDecoder.class);

    private final ErrorDecoder defaultErrorDecoder = new ResponseErrorDecoder();

    @Override
    public Exception decode(String methodKey, Response response) {
        var  feignResponseException = (FeignResponseException) defaultErrorDecoder.decode(methodKey, response);

        if (feignResponseException.getStatusCode() == 403) {
            String url = response.request() != null ? response.request().url() : "<unknown>";
            return new VaultPermissionDeniedException("Permission denied calling Vault: " + feignResponseException.getMessage(), feignResponseException);
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}

