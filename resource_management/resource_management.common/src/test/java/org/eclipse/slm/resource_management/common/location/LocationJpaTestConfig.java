package org.eclipse.slm.resource_management.common.location;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LocationJpaTestConfig {
    public static Location locationWithoutUuid = new Location("location-without-uuid");
    public static Location locationWithUuid = new Location(
            UUID.fromString("966eeaa3-5487-4323-a5e6-20aad892d8fa"),
            "location-with-uuid"
    );

    public static List<Location> locationList;

   static {
        locationList = new ArrayList<>(){{
            add(locationWithoutUuid);
            add(locationWithUuid);
        }};
    }
}
