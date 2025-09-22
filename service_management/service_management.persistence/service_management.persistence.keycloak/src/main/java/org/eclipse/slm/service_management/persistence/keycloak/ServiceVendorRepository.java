package org.eclipse.slm.service_management.persistence.keycloak;

import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.KeycloakAdminClient;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceVendorJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServiceVendorRepository {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceVendorRepository.class);

    private final ServiceVendorJpaRepository serviceVendorJpaRepository;

    private final KeycloakAdminClient keycloakAdminClient;

    @Autowired
    public ServiceVendorRepository(ServiceVendorJpaRepository serviceVendorJpaRepository,
                                   KeycloakAdminClient keycloakAdminClient) {
        this.serviceVendorJpaRepository = serviceVendorJpaRepository;
        this.keycloakAdminClient = keycloakAdminClient;
    }

    public List<ServiceVendor> getServiceVendors() {
        var serviceVendors = serviceVendorJpaRepository.findAll();;
        return serviceVendors;
    }

    public Optional<ServiceVendor> getServiceVendorById(UUID serviceVendorId) {
        var serviceVendorOptional = serviceVendorJpaRepository.findById(serviceVendorId);
        return serviceVendorOptional;
    }

    public byte[] getServiceVendorLogo(UUID serviceVendorId) throws ServiceVendorNotFoundException {
        var serviceVendorOptional = serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            return serviceVendorOptional.get().getLogo();
        }
        else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }

    private ServiceVendor createServiceVendor(ServiceVendor serviceVendor, String keycloakRealm) {
        var keycloakGroupName = serviceVendor.getKeycloakGroupName();
        var groupAttributes = new HashMap<String, List<String>>();
        this.keycloakAdminClient.createGroup(
                keycloakRealm,
                keycloakGroupName,
                groupAttributes);
        serviceVendor = this.serviceVendorJpaRepository.save(serviceVendor);
        LOG.info("Service Vendor with id '" + serviceVendor.getId() + "' created");

        return serviceVendor;
    }

    public ServiceVendor createOrUpdateServiceVendorWithId(ServiceVendor serviceVendor, String keycloakRealm) {
        var savedServiceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendor.getId());
        if (savedServiceVendorOptional.isPresent())
        {
            var savedServiceVendor = savedServiceVendorOptional.get();
            var serviceVendorUpdate = ObjectMapperUtils.map(serviceVendor, savedServiceVendor);
            serviceVendor = this.serviceVendorJpaRepository.save(serviceVendorUpdate);
            LOG.info("Service Vendor with id '" + serviceVendor.getId() + "' updated");
        }
        else
        {
            serviceVendor = this.createServiceVendor(serviceVendor, keycloakRealm);
        }

        return serviceVendor;
    }

    public void deleteServiceVendorById(UUID serviceVendorId, String keycloakRealm)
            throws ServiceVendorNotFoundException, KeycloakGroupNotFoundException {
        var serviceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isEmpty()) {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
        else {
            var serviceVendor = serviceVendorOptional.get();
            this.serviceVendorJpaRepository.delete(serviceVendor);
            this.keycloakAdminClient.deleteGroup(
                    keycloakRealm,
                    serviceVendor.getKeycloakGroupName());
        }
    }

    public List<ServiceVendorDeveloper> getDevelopersOfServiceVendor(UUID serviceVendorId, String keycloakRealm)
            throws KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
        var serviceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            var usersOfServiceVendorKeycloakGroup = this.keycloakAdminClient.getUsersOfGroup(
                    keycloakRealm,
                    serviceVendorOptional.get().getKeycloakGroupName());

            var developers = new ArrayList<ServiceVendorDeveloper>();
            for (var user : usersOfServiceVendorKeycloakGroup)
            {
                var developer = new ServiceVendorDeveloper();
                developer.setId(UUID.fromString(user.getId()));
                developer.setUsername(user.getUsername());
                developer.setFirstName(user.getFirstName());
                developer.setLastName(user.getLastName());
                developer.setEmail(user.getEmail());
                developers.add(developer);
            }

            return developers;
        }
        else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }

    public void addDeveloperToServiceVendor(UUID serviceVendorId, UUID userId, String keycloakRealm)
            throws KeycloakUserNotFoundException, KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
        var serviceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            this.keycloakAdminClient.assignUserToGroup(
                    keycloakRealm,
                    serviceVendorOptional.get().getKeycloakGroupName(),
                    userId);

        } else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }

    public void removeDeveloperFromServiceVendor(UUID serviceVendorId, UUID userId, String keycloakRealm)
            throws ServiceVendorNotFoundException, KeycloakUserNotFoundException, KeycloakGroupNotFoundException {
        var serviceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            this.keycloakAdminClient.removeUserFromGroup(
                    keycloakRealm,
                    serviceVendorOptional.get().getKeycloakGroupName(),
                    userId);
        } else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }

    public List<UUID> getServiceVendorsOfDeveloper(JwtAuthenticationToken jwtAuthenticationToken) {
        var token = jwtAuthenticationToken.getToken();
        var otherClaims = token.getClaims();

        var serviceVendorIds = new ArrayList<UUID>();
        if (otherClaims.containsKey("groups")) {
            var userGroups = otherClaims.get("groups");
            List<String> userGroupsCasted;
            if (userGroups instanceof String[]) {
                userGroupsCasted = List.of((String[])userGroups);
            } else {
                userGroupsCasted = (ArrayList<String>)userGroups;
            }
            for (var userGroup : userGroupsCasted) {
                if (userGroup.startsWith("vendor_")) {
                    var serviceVendorId = UUID.fromString(userGroup.toString().replace("vendor_", ""));
                    serviceVendorIds.add(serviceVendorId);
                }
            }

        }

        return serviceVendorIds;
    }
}
