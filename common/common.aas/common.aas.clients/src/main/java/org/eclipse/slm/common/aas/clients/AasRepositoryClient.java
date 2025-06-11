package org.eclipse.slm.common.aas.clients;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.basyx.aasrepository.client.ConnectedAasRepository;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingSubmodelReferenceException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AasRepositoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(AasRepositoryClient.class);

    private final String aasRepositoryUrl;

    private final ConnectedAasRepository connectedAasRepository;

    public AasRepositoryClient(@Value("${aas.aas-repository.url}") String aasRepositoryUrl) {
        this.aasRepositoryUrl = aasRepositoryUrl;
        this.connectedAasRepository = new ConnectedAasRepository(this.aasRepositoryUrl);
    }

    public void createOrUpdateAas(AssetAdministrationShell aas) {
        try {
            this.connectedAasRepository.createAas(aas);
        } catch (CollidingIdentifierException e) {
            this.connectedAasRepository.updateAas(aas.getId(), aas);
        }
        catch (RuntimeException e) {
            LOG.error(e.getMessage());
        }
    }

    public AssetAdministrationShell getAas(String aasId) {
        var aas = this.connectedAasRepository.getAas(aasId);

        return aas;
    }

    public void addSubmodelReferenceToAas(String aasId, Submodel submodel) {
        this.addSubmodelReferenceToAas(aasId, submodel.getId());
    }

    public void addSubmodelReferenceToAas(String aasId, String smId) {
        try {
            var submodelReference = new DefaultReference.Builder()
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.SUBMODEL)
                            .value(smId).build())
                    .build();
            this.connectedAasRepository.addSubmodelReference(aasId, submodelReference);
        } catch (CollidingSubmodelReferenceException e) {
            LOG.debug("Submodel reference already exists");
        } catch (ElementDoesNotExistException e) {
            LOG.error("AAS with id {} does not exist", aasId);
        }
    }

    public void removeSubmodelReferenceFromAas(String aasId, String smId) {
        this.connectedAasRepository.removeSubmodelReference(aasId, smId);
    }

    public void deleteAAS(String aasId) {
        this.connectedAasRepository.deleteAas(aasId);
    }
}
