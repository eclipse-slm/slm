package org.eclipse.slm.resource_management.service.rest.resources;

import org.eclipse.slm.resource_management.model.resource.Location;
import org.assertj.core.util.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocationJpaTestConfig {
    Location locationWithoutUuid = new Location("location-without-uuid");
    Location locationWithUuid = new Location(
            UUID.fromString("966eeaa3-5487-4323-a5e6-20aad892d8fa"),
            "location-with-uuid"
    );

    List<Location> locationList;

    public LocationJpaTestConfig() {
        locationList = new ArrayList<>(){{
            add(locationWithoutUuid);
            add(locationWithUuid);
        }};
    }
}
