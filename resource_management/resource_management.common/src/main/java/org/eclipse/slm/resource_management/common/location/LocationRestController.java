package org.eclipse.slm.resource_management.common.location;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.slm.resource_management.common.exceptions.LocationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/resources/locations")
@Tag(name = "Location")
public class LocationRestController {
    private final static Logger LOG = LoggerFactory.getLogger(LocationRestController.class);

    private final LocationHandler locationHandler;

    public LocationRestController(LocationHandler locationHandler) {
        this.locationHandler = locationHandler;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @Operation(summary = "Get locations")
    public List<Location> getLocations(
            @RequestParam(name = "id", required = false)    Optional<UUID> id
    ) throws LocationNotFoundException {
        if(id.isPresent()) {
            return List.of(locationHandler.getLocationById(id.get()));
        } else
            return locationHandler.getLocations();
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    @Operation(summary = "Add new Location")
    public ResponseEntity<Location> addLocation(
            @RequestParam(name = "name", required = true)   String name
    ){
        var locationToAdd = new Location(name);
        var addedLocation = this.locationHandler.addLocation(locationToAdd);

        return new ResponseEntity<>(addedLocation, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{locationId}", method = RequestMethod.PUT)
    @Operation(summary = "Add new Location with id")
    public ResponseEntity<Location> addLocation(
            @PathVariable(name = "locationId")  UUID locationId,
            @RequestParam(name = "name")        String name
    ){
        var locationToAdd = new Location(locationId, name);
        var addedLocation = this.locationHandler.addLocation(locationToAdd);

        return new ResponseEntity<>(addedLocation, HttpStatus.CREATED);
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @Operation(summary = "Delete Location")
    public ResponseEntity<Void> deleteLocation(
            @RequestParam(name = "id")    UUID id
    ) {
        this.locationHandler.deleteLocation(id);

        return ResponseEntity.ok().build();
    }
}
