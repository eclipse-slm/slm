package org.eclipse.slm.common.aas.repositories.submodels;

public interface SubmodelRepositoryFactory {

    SubmodelRepository getSubmodelRepository(String aasId);

}
