package org.eclipse.slm.common.aas.repositories;

import org.eclipse.digitaltwin.basyx.submodelrepository.SubmodelRepository;

public interface SubmodelRepositoryFactory {

    SubmodelRepository getSubmodelRepository(String aasId);

}
