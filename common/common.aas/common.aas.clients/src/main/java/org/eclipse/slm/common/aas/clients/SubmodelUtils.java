package org.eclipse.slm.common.aas.clients;

import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelDescriptor;

import java.util.List;
import java.util.Optional;

public class SubmodelUtils {

    public static Optional<SubmodelElement> findSubmodelElement(List<SubmodelElement> submodelElements, String smeIdShortPath) {
        String idOfNextSubmodelElement;
        var pathSegments = smeIdShortPath.split("\\.");
        if (pathSegments.length > 1) {
            idOfNextSubmodelElement = pathSegments[0];
            smeIdShortPath = smeIdShortPath.replaceFirst(idOfNextSubmodelElement + ".", "");
        }
        else {
            idOfNextSubmodelElement = smeIdShortPath;
        }

        var submodelElementOptional = submodelElements.stream()
                .filter(sme -> sme.getIdShort().equals(idOfNextSubmodelElement)).findAny();
        if (submodelElementOptional.isPresent()) {
            var submodelElement = submodelElementOptional.get();
            if (submodelElement instanceof SubmodelElementCollection) {
                var nestedSubmodelElements = ((SubmodelElementCollection)submodelElement).getValue();
                return SubmodelUtils.findSubmodelElement(nestedSubmodelElements, smeIdShortPath);
            }
            else {
                return Optional.of(submodelElement);
            }
        }

        return Optional.empty();
    }

    public static SubmodelDescriptor createSubmodelDescriptorForSubmodel(
            Submodel submodel,
            String endpointScheme,
            String endpointUrl
    ) {

        var submodelDescriptorBuilder = new DefaultSubmodelDescriptor.Builder()
                .id(submodel.getId())
                .idShort(submodel.getIdShort())
                .semanticId(submodel.getSemanticId())
                .supplementalSemanticId(submodel.getSupplementalSemanticIds())
                .description(submodel.getDescription())
                .displayName(submodel.getDisplayName())
                .extensions(submodel.getExtensions())
                .administration(submodel.getAdministration());

        var endpoint = new DefaultEndpoint.Builder()
                ._interface("SUBMODEL-3.0")
                .protocolInformation(new DefaultProtocolInformation.Builder()
                        .endpointProtocol(endpointScheme)
                        .href(endpointUrl)
                        .build()
                )
                .build();

        submodelDescriptorBuilder.endpoints(endpoint);

        return submodelDescriptorBuilder.build();
    }
}
