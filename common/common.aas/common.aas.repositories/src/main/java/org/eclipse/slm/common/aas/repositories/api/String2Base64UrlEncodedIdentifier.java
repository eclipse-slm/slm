package org.eclipse.slm.common.aas.repositories.api;

import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Handles conversion from a string representing a Base64Url encoded value to
 * {@link Base64UrlEncodedIdentifier}
 * 
 * @author gordt, schnicke
 *
 */
@Component
public class String2Base64UrlEncodedIdentifier implements Converter<String, Base64UrlEncodedIdentifier> {

	@Override
	public Base64UrlEncodedIdentifier convert(String source) {
		return Base64UrlEncodedIdentifier.fromEncodedValue(source);
	}
}