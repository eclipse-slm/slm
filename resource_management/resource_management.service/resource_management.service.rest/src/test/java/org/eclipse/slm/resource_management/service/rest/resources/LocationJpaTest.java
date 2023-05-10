package org.eclipse.slm.resource_management.service.rest.resources;

import org.eclipse.slm.resource_management.model.resource.Location;
import org.eclipse.slm.resource_management.persistence.api.LocationJpaRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocationJpaTest {
    @Autowired
    LocationJpaRepository locationJpaRepository;
    LocationJpaTestConfig config = new LocationJpaTestConfig();

    @Test
    @Order(10)
    public void getAllLocationsExpectEmptyResult() {
        List<Location> locations = locationJpaRepository.findAll();

        assertEquals(0, locations.size());
    }

    @Test
    @Order(20)
    public void saveLocationWithoutDefinedUuid() {
        List<Location> locationsBefore = locationJpaRepository.findAll();

        locationJpaRepository.save(config.locationWithoutUuid);

        List<Location> locationsAfter = locationJpaRepository.findAll();

        assertEquals(locationsBefore.size()+1, locationsAfter.size());
        assertTrue(
                locationsAfter.contains(config.locationWithoutUuid)
        );
    }

    @Test
    @Order(30)
    public void saveLocationWithDefinedUuid() {
        List<Location> locationsBefore = locationJpaRepository.findAll();

        locationJpaRepository.save(config.locationWithUuid);

        List<Location> locationsAfter = locationJpaRepository.findAll();

        assertEquals(locationsBefore.size()+1, locationsAfter.size());
        assertTrue(
                locationsAfter.contains(config.locationWithUuid)
        );
    }

    @Test
    @Order(40)
    public void getLocationByUuidExpectOneResult() {
        locationJpaRepository.save(config.locationWithoutUuid);
        Optional<Location> optionalLocation = locationJpaRepository.findById(
                config.locationWithoutUuid.getId()
        );

        assertTrue(optionalLocation.isPresent());
        assertEquals(
                config.locationWithoutUuid.getName(),
                optionalLocation.get().getName()
        );
    }

    @Test
    @Order(50)
    public void createListOfLocationsAndDeleteAllLocationsExpectNoResult() {
        locationJpaRepository.saveAll(config.locationList);

        List<Location> locationsBefore = locationJpaRepository.findAll();

        assertEquals(config.locationList.size(), locationsBefore.size());

        locationJpaRepository.deleteAll();

        List<Location> locationsAfter = locationJpaRepository.findAll();

        assertEquals(0, locationsAfter.size());
    }
}
