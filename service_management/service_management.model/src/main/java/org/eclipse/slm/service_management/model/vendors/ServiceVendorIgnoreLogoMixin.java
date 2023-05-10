package org.eclipse.slm.service_management.model.vendors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class ServiceVendorIgnoreLogoMixin {
    @JsonIgnore
    public abstract String getLogo();
}
