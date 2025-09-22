package org.eclipse.slm.resource_management.common.location;

import org.eclipse.slm.resource_management.common.exceptions.LocationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LocationHandler {

    private final static Logger LOG = LoggerFactory.getLogger(LocationRestController.class);

    private final LocationJpaRepository locationJpaRepository;

    public LocationHandler(LocationJpaRepository locationJpaRepository) {
        this.locationJpaRepository = locationJpaRepository;
    }

    public List<Location> getLocations() {
        return this.locationJpaRepository.findAll();
    }

    public Location getLocationById(UUID id) throws LocationNotFoundException {
        Optional<Location> optionalLocation = locationJpaRepository.findById(id);

        if (optionalLocation.isPresent()) {
            return optionalLocation.get();
        } else {
            throw new LocationNotFoundException(id);
        }
    }

    public Location addLocation(Location location) {
        return locationJpaRepository.save(location);
    }

    public void deleteLocation(UUID id) {
        locationJpaRepository.deleteById(id);
    }

}
