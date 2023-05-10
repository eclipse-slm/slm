package org.eclipse.slm.resource_management.service.rest.location;

import org.eclipse.slm.resource_management.model.resource.Location;
import org.eclipse.slm.resource_management.persistence.api.LocationJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/resources/locations")
public class LocationRestController {
    private final static Logger LOG = LoggerFactory.getLogger(LocationRestController.class);
    private LocationJpaRepository locationJpaRepository;

    @Autowired
    public void LocationRestController(LocationJpaRepository locationJpaRepository) {
        this.locationJpaRepository = locationJpaRepository;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get locations")
    public List<Location> getLocations(
            @RequestParam(name = "id", required = false)    Optional<UUID> id
    ) {
        if(id.isPresent()) {
            Optional<Location> optionalLocation = locationJpaRepository.findById(id.get());
            return Arrays.asList(optionalLocation.get());
        } else
            return locationJpaRepository.findAll();
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Create new Location")
    public ResponseEntity addLocation(
            @RequestParam(name = "id", required = false)    Optional<UUID> optionalId,
            @RequestParam(name = "name", required = true)   String name
    ){
        Location location;
        if(optionalId.isEmpty())
            location = new Location(name);
        else
            location = new Location(optionalId.get(), name);

        locationJpaRepository.save(location);

        return ResponseEntity.created(null).build();
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @Operation(summary = "Delete Location")
    public ResponseEntity<Object> deleteLocation(
            @RequestParam(name = "id")    UUID id
    ) {
        locationJpaRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
