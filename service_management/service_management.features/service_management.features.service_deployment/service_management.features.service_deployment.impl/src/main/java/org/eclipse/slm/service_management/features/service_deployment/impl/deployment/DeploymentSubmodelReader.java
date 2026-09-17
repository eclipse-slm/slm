package org.eclipse.slm.service_management.features.service_deployment.impl.deployment;

import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.slm.aas.clients.shellrepository.AasRepositoryClient;
import org.eclipse.slm.aas.clients.submodelrepository.SubmodelRepositoryClientFactory;
import org.eclipse.slm.common.aas.submodels.deployment.DeploymentSubmodelTemplate;
import org.eclipse.slm.common.model.DeploymentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Liest ein Deployment-Submodel hinter einem Registry-Deskriptor aus und loest
 * die AAS-Referenz zum Anzeigenamen auf.
 */
@Component
public class DeploymentSubmodelReader {

    private final static Logger LOG = LoggerFactory.getLogger(DeploymentSubmodelReader.class);

    private final AasRepositoryClient aasRepositoryClient;

    public DeploymentSubmodelReader(AasRepositoryClient aasRepositoryClient) {
        this.aasRepositoryClient = aasRepositoryClient;
    }

    public Optional<DeploymentTarget> read(SubmodelDescriptor descriptor) {
        try {
            var endpoint = descriptor.getEndpoints().get(0).getProtocolInformation().getHref();
            var client = SubmodelRepositoryClientFactory.FromSubmodelDescriptor(descriptor, null);
            var submodel = client.getSubmodelOrThrow(descriptor.getId());

            var aasId = this.aasIdFrom(submodel);
            var mechanism = this.collection(submodel, DeploymentSubmodelTemplate.SMC_MECHANISM);

            return Optional.of(new DeploymentTarget(
                    submodel.getId(),
                    endpoint,
                    aasId,
                    this.displayNameFor(aasId, submodel.getIdShort()),
                    this.propertyValue(mechanism, DeploymentSubmodelTemplate.SME_MECHANISM_NAME),
                    this.propertyValue(mechanism, DeploymentSubmodelTemplate.SME_MECHANISM_VERSION),
                    this.supportedDeploymentTypes(submodel),
                    this.mechanismProperties(mechanism)));
        } catch (Exception e) {
            LOG.warn("Deployment submodel '{}' could not be read and is skipped: {}",
                    descriptor.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    private String aasIdFrom(Submodel submodel) {
        for (var element : submodel.getSubmodelElements()) {
            if (element instanceof ReferenceElement referenceElement
                    && DeploymentSubmodelTemplate.SME_ASSET_ADMINISTRATION_SHELL.equals(element.getIdShort())) {
                return referenceElement.getValue().getKeys().get(0).getValue();
            }
        }
        throw new IllegalStateException("Deployment submodel '" + submodel.getId()
                + "' has no " + DeploymentSubmodelTemplate.SME_ASSET_ADMINISTRATION_SHELL + " reference");
    }

    private String displayNameFor(String aasId, String fallback) {
        try {
            return this.aasRepositoryClient.getAas(aasId)
                    .map(aas -> aas.getIdShort() == null ? aasId : aas.getIdShort())
                    .orElse(fallback);
        } catch (Exception e) {
            LOG.debug("AAS '{}' could not be resolved for a display name: {}", aasId, e.getMessage());
            return fallback;
        }
    }

    private SubmodelElementCollection collection(Submodel submodel, String idShort) {
        for (var element : submodel.getSubmodelElements()) {
            if (element instanceof SubmodelElementCollection collection && idShort.equals(element.getIdShort())) {
                return collection;
            }
        }
        throw new IllegalStateException("Deployment submodel has no collection '" + idShort + "'");
    }

    private String propertyValue(SubmodelElementCollection collection, String idShort) {
        for (var element : collection.getValue()) {
            if (element instanceof Property property && idShort.equals(element.getIdShort())) {
                return property.getValue() == null ? "" : property.getValue();
            }
        }
        return "";
    }

    private Map<String, String> mechanismProperties(SubmodelElementCollection mechanism) {
        var properties = new LinkedHashMap<String, String>();
        for (var element : mechanism.getValue()) {
            if (element instanceof SubmodelElementCollection collection
                    && DeploymentSubmodelTemplate.SMC_MECHANISM_PROPERTIES.equals(element.getIdShort())) {
                for (var property : collection.getValue()) {
                    if (property instanceof Property typed && typed.getValue() != null) {
                        properties.put(typed.getIdShort(), typed.getValue());
                    }
                }
            }
        }
        return properties;
    }

    private List<DeploymentType> supportedDeploymentTypes(Submodel submodel) {
        var types = new ArrayList<DeploymentType>();
        for (var element : submodel.getSubmodelElements()) {
            if (element instanceof SubmodelElementList list
                    && DeploymentSubmodelTemplate.SML_SUPPORTED_DEPLOYMENT_TYPES.equals(element.getIdShort())) {
                for (var entry : list.getValue()) {
                    if (entry instanceof Property property && property.getValue() != null) {
                        try {
                            types.add(DeploymentType.valueOf(property.getValue()));
                        } catch (IllegalArgumentException e) {
                            LOG.debug("Unknown deployment type '{}' declared by '{}', ignored",
                                    property.getValue(), submodel.getId());
                        }
                    }
                }
            }
        }
        return types;
    }
}
