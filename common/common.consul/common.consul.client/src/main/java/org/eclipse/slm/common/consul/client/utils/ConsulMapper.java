
package org.eclipse.slm.common.consul.client.utils;

import org.eclipse.slm.common.consul.model.acl.*;
import org.eclipse.slm.common.consul.model.catalog.CatalogRegistration;
import org.eclipse.slm.common.consul.model.catalog.Node;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Mapper for Consul objects.
 * Uses MapStruct to generate the mapping implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface ConsulMapper {

    ConsulMapper INSTANCE = Mappers.getMapper(ConsulMapper.class);

    @Mapping(source = "meta", target = "nodeMeta")
    CatalogRegistration toCatalogRegistration(Node source);

    PolicyCreateRequest toCreateRequest(Policy policy);

    PolicyUpdateRequest tuUpdateRequest(Policy policy);

    RoleUpdateRequest toUpdateRequest(Role updatedRole);
}
