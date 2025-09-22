package org.eclipse.slm.resource_management.features.capabilities.jobs;

import java.util.EnumSet;
import java.util.List;

public enum CapabilityJobState {
    CREATED,
    INSTALLING,
    INSTALLED,
    UNINSTALLING,
    UNINSTALLED,
    FAILED;

    public static EnumSet<CapabilityJobState> getEndStates() {
        return EnumSet.of(UNINSTALLED, FAILED);
    }

    public static EnumSet<CapabilityJobState> getStableStates() {
        var stableStates = CapabilityJobState.getEndStates();
        stableStates.add(INSTALLED);
        return stableStates;
    }
}
