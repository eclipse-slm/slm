package org.eclipse.slm.common.aas.repositories.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubmodelNotFoundException extends RuntimeException {

	public SubmodelNotFoundException(String resourceId) {
		super("Submodel for resource with id='" + resourceId + "' not found");
	}
}
