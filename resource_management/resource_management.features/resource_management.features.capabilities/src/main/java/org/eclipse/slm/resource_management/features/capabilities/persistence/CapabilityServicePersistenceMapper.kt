package org.eclipse.slm.resource_management.features.capabilities.persistence

import org.eclipse.slm.resource_management.features.capabilities.exceptions.CapabilityNotFoundException
import org.eclipse.slm.resource_management.features.capabilities.model.SingleHostCapabilityService
import org.springframework.stereotype.Component

@Component
class CapabilityServicePersistenceMapper(
    private val capabilityJpaRepository: CapabilityJpaRepository
) {

    fun toEntity(service: SingleHostCapabilityService): CapabilityServiceEntity {
        val entity = CapabilityServiceEntity(service.serviceId)
        entity.resourceId = service.resourceId
        entity.capabilityId = service.capability.id
        entity.status = service.status
        entity.managed = service.managed
        entity.port = service.port
        entity.customMeta = HashMap(service.customMeta)
        entity.serviceClass = CapabilityServiceClass.SINGLE_HOST
        return entity
    }

    fun toSingleHostDomain(entity: CapabilityServiceEntity): SingleHostCapabilityService {
        val capabilityId = entity.capabilityId
            ?: throw CapabilityNotFoundException(entity.id)
        val capability = capabilityJpaRepository.findById(capabilityId)
            .orElseThrow { CapabilityNotFoundException(capabilityId) }
        val service = SingleHostCapabilityService(
            entity.resourceId!!,
            entity.id,
            capability,
            entity.status,
            entity.managed,
            HashMap(entity.customMeta)
        )
        service.port = entity.port
        return service
    }
}
