package org.eclipse.slm.common.aas.repositories.shells;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

public interface AasFactory {

	AssetAdministrationShell createAas(String aasId);

}
