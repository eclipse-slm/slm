package org.eclipse.slm.common.aas.repositories.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AasNotFoundException extends RuntimeException {

	public AasNotFoundException(String aasId) {
		super("AAS [id='" + aasId + "'] not found");
	}
}
