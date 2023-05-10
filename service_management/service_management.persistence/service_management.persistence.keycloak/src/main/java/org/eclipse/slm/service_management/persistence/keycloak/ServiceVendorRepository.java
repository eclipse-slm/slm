package org.eclipse.slm.service_management.persistence.keycloak;

import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakGroupNotFoundException;
import org.eclipse.slm.common.keycloak.config.KeycloakUtil;
import org.eclipse.slm.common.keycloak.config.exceptions.KeycloakUserNotFoundException;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;
import org.eclipse.slm.service_management.model.vendors.ServiceVendorDeveloper;
import org.eclipse.slm.service_management.model.vendors.exceptions.ServiceVendorNotFoundException;
import org.eclipse.slm.service_management.persistence.api.ServiceVendorJpaRepository;
import org.keycloak.KeycloakPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ServiceVendorRepository {

    public final static Logger LOG = LoggerFactory.getLogger(ServiceVendorRepository.class);

    private final ServiceVendorJpaRepository serviceVendorJpaRepository;

    private final KeycloakUtil keycloakUtil;

    @Autowired
    public ServiceVendorRepository(ServiceVendorJpaRepository serviceVendorJpaRepository,
                                   KeycloakUtil keycloakUtil) {
        this.serviceVendorJpaRepository = serviceVendorJpaRepository;
        this.keycloakUtil = keycloakUtil;
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
        this.keycloakUtil.createGroup(
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
            this.keycloakUtil.deleteGroup(
                    keycloakRealm,
                    serviceVendor.getKeycloakGroupName());
        }
    }

    public List<ServiceVendorDeveloper> getDevelopersOfServiceVendor(UUID serviceVendorId, String keycloakRealm)
            throws KeycloakGroupNotFoundException, ServiceVendorNotFoundException {
        var serviceVendorOptional = this.serviceVendorJpaRepository.findById(serviceVendorId);
        if (serviceVendorOptional.isPresent()) {
            var usersOfServiceVendorKeycloakGroup = this.keycloakUtil.getUsersOfGroup(
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
            this.keycloakUtil.assignUserToGroup(
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
            this.keycloakUtil.removeUserFromGroup(
                    keycloakRealm,
                    serviceVendorOptional.get().getKeycloakGroupName(),
                    userId);
        } else {
            throw new ServiceVendorNotFoundException(serviceVendorId);
        }
    }

    public List<UUID> getServiceVendorsOfDeveloper(KeycloakPrincipal keycloakPrincipal) {
        var token = keycloakPrincipal.getKeycloakSecurityContext().getToken();
        var otherClaims = token.getOtherClaims();

        var serviceVendorIds = new ArrayList<UUID>();
        if (otherClaims.containsKey("groups")) {
            var userGroups = (ArrayList) otherClaims.get("groups");
            for (var userGroup : userGroups) {
                if (userGroup.toString().startsWith("vendor_")) {
                    var serviceVendorId = UUID.fromString(userGroup.toString().replace("vendor_", ""));
                    serviceVendorIds.add(serviceVendorId);
                }
            }

        }

        return serviceVendorIds;
    }
}
