package org.eclipse.slm.common.aas.repositories.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubmodelNotFoundException extends RuntimeException {

	public SubmodelNotFoundException(String aasId, String submodelId) {
		super("Submodel [id='" + submodelId + "'for AAS [id='" + aasId + "'] not found");
	}

	public SubmodelNotFoundException(String submodelId) {
		super("Submodel [id='" + submodelId + "' not found");
	}
}
