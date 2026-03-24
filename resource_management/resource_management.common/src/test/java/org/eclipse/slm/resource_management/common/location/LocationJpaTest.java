package org.eclipse.slm.resource_management.common.location;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class LocationJpaTest {

    @Autowired
    private LocationJpaRepository locationJpaRepository;

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

        locationJpaRepository.save(LocationJpaTestConfig.locationWithoutUuid);

        List<Location> locationsAfter = locationJpaRepository.findAll();

        assertEquals(locationsBefore.size()+1, locationsAfter.size());
        assertTrue(
                locationsAfter.contains(LocationJpaTestConfig.locationWithoutUuid)
        );
    }

    @Test
    @Order(30)
    public void saveLocationWithDefinedUuid() {
        List<Location> locationsBefore = locationJpaRepository.findAll();

        locationJpaRepository.save(LocationJpaTestConfig.locationWithUuid);

        List<Location> locationsAfter = locationJpaRepository.findAll();

        assertEquals(locationsBefore.size()+1, locationsAfter.size());
        assertTrue(
                locationsAfter.contains(LocationJpaTestConfig.locationWithUuid)
        );
    }

    @Test
    @Order(40)
    public void getLocationByUuidExpectOneResult() {
        locationJpaRepository.save(LocationJpaTestConfig.locationWithoutUuid);
        Optional<Location> optionalLocation = locationJpaRepository.findById(
                LocationJpaTestConfig.locationWithoutUuid.getId()
        );

        assertTrue(optionalLocation.isPresent());
        assertEquals(
                LocationJpaTestConfig.locationWithoutUuid.getName(),
                optionalLocation.get().getName()
        );
    }

    @Test
    @Order(50)
    public void createListOfLocationsAndDeleteAllLocationsExpectNoResult() {
        locationJpaRepository.saveAll(LocationJpaTestConfig.locationList);

        List<Location> locationsBefore = locationJpaRepository.findAll();

        assertEquals(LocationJpaTestConfig.locationList.size(), locationsBefore.size());

        locationJpaRepository.deleteAll();

        List<Location> locationsAfter = locationJpaRepository.findAll();

        assertEquals(0, locationsAfter.size());
    }
}
