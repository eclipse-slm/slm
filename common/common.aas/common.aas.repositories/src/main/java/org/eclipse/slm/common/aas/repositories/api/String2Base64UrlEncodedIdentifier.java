package org.eclipse.slm.common.aas.repositories.api;

import org.eclipse.digitaltwin.basyx.http.Base64UrlEncodedIdentifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class String2Base64UrlEncodedIdentifier implements Converter<String, Base64UrlEncodedIdentifier> {

	@Override
	public Base64UrlEncodedIdentifier convert(String source) {
		return Base64UrlEncodedIdentifier.fromEncodedValue(source);
	}
}