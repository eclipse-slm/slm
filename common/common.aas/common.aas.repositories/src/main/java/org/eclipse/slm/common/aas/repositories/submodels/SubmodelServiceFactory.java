package org.eclipse.slm.common.aas.repositories.submodels;

import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;

public interface SubmodelServiceFactory {

	SubmodelService getSubmodelService(String aasId);
}
