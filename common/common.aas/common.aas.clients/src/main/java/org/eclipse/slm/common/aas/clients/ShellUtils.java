package org.eclipse.slm.common.aas.clients;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShellDescriptor;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;

public class ShellUtils {

    public static AssetAdministrationShellDescriptor createShellDescriptorForShell(
            AssetAdministrationShell aas,
            String endpointScheme,
            String endpointUrl
    ) {

        var aasDescriptorBuilder = new DefaultAssetAdministrationShellDescriptor.Builder()
                .id(aas.getId())
                .idShort(aas.getIdShort())
                .description(aas.getDescription())
                .displayName(aas.getDisplayName())
                .extensions(aas.getExtensions())
                .administration(aas.getAdministration());

        if (aas.getAssetInformation() != null) {
            aasDescriptorBuilder
                    .globalAssetId(aas.getAssetInformation().getGlobalAssetId())
                    .assetKind(aas.getAssetInformation().getAssetKind())
                    .assetType(aas.getAssetInformation().getAssetType())
                    .specificAssetIds(aas.getAssetInformation().getSpecificAssetIds());
        }

        var endpoint = new DefaultEndpoint.Builder()
                ._interface("AAS-3.0")
                .protocolInformation(new DefaultProtocolInformation.Builder()
                        .endpointProtocol(endpointScheme)
                        .href(endpointUrl)
                    .build()
                )
            .build();

        aasDescriptorBuilder.endpoints(endpoint);

        return aasDescriptorBuilder.build();
    }

}
