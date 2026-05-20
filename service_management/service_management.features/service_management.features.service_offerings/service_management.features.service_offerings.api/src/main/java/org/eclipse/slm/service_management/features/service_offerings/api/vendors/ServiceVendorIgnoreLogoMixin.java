package org.eclipse.slm.service_management.features.service_offerings.api.vendors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class ServiceVendorIgnoreLogoMixin {
    @JsonIgnore
    public abstract String getLogo();
}

