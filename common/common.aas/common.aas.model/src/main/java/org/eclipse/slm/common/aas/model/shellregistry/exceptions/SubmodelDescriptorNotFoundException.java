package org.eclipse.slm.common.aas.model.shellregistry.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubmodelDescriptorNotFoundException extends RuntimeException {

    public SubmodelDescriptorNotFoundException(String aasId, String submodelId) {
        super(String.format("Submodel Descriptor [id='%s'] of AAS [id='%s'] not found", submodelId, aasId));
    }
}
