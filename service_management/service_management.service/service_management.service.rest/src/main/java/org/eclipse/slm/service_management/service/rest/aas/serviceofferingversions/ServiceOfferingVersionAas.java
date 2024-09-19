package org.eclipse.slm.service_management.service.rest.aas.serviceofferingversions;

import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingVersion;

import java.util.UUID;

public class ServiceOfferingVersionAas extends DefaultAssetAdministrationShell {

    public static final String AAS_ID_PREFIX = "ServiceOfferingVersion_";

    public ServiceOfferingVersionAas(ServiceOfferingVersion serviceOfferingVersion) {
        this.id = AAS_ID_PREFIX + serviceOfferingVersion.getId();
        this.idShort = AAS_ID_PREFIX + serviceOfferingVersion.getServiceOffering().getName() + "-" + serviceOfferingVersion.getVersion();
    }

    public static String createAasIdFromServiceOfferingVersionId(UUID resourceId) {
        return AAS_ID_PREFIX + resourceId;
    }
}
