package org.eclipse.slm.resource_management.common.aas.submodels;

import org.eclipse.slm.aas.repositories.submodels.SubmodelRepositoryFactory;

/**
 * Von Feature-Modulen implementiert, die zusaetzliche Submodels an die AAS einer Resource haengen.
 * Alle Spring-Beans dieses Typs werden automatisch in das ResourcesSubmodelRepository eingehaengt,
 * ohne dass resource_management.common die Feature-Module kennen muss.
 */
public interface ResourceSubmodelContributor extends SubmodelRepositoryFactory {

    /**
     * Stabiler Schluessel, unter dem der Beitrag registriert wird. Muss ueber alle
     * Contributor hinweg eindeutig sein.
     */
    String getContributorKey();
}
