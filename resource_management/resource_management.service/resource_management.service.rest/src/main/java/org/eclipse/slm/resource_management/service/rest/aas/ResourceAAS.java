package org.eclipse.slm.resource_management.service.rest.aas;

import org.eclipse.slm.resource_management.model.resource.BasicResource;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;

import java.util.UUID;

public class ResourceAAS extends AssetAdministrationShell {

    public static IIdentifier createIdentification(UUID uuid) {
        return new CustomId(uuid.toString());
    }

    public ResourceAAS(BasicResource resource) {
        super();
        this.setIdentification(createIdentification(resource.getId()));
        this.setIdShort(resource.getHostname());
        var asset = new Asset();
        asset.setIdShort(resource.getHostname());
        this.setAsset(asset);
    }
}
