package org.eclipse.slm.resource_management.service.importer;

import java.io.InputStream;
import java.util.*;

public class ImportDefinition {

    public Map<UUID, String> locations = new HashMap<>();

    public List<ImportDevice> devices = new ArrayList<>();

    public Map<UUID, List<InputStream>> aasxFiles = new HashMap<>();

    public Map<UUID, String> getLocations() {
        return locations;
    }

    public void addLocation(UUID locationId, String locationName) {
        locations.put(locationId, locationName);
    }

    public void setLocations(Map<UUID, String> locations) {
        this.locations = locations;
    }

    public List<ImportDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<ImportDevice> devices) {
        this.devices = devices;
    }

    public Map<UUID, List<InputStream>> getAasxFiles() {
        return aasxFiles;
    }

    public void setAasxFiles(Map<UUID, List<InputStream>> aasxFiles) {
        this.aasxFiles = aasxFiles;
    }
}
