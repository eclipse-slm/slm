package org.eclipse.slm.common.consul.model.catalog

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

class CatalogService {
    @JsonProperty("ID")
    var id: UUID? = null

    @JsonProperty("Node")
    var node: String? = null

    @JsonProperty("Address")
    var address: String? = null

    @JsonProperty("Datacenter")
    var datacenter: String? = null

    @JsonProperty("TaggedAddresses")
    var taggedAddresses: TaggedAddresses? = null

    @JsonProperty("NodeMeta")
    var nodeMeta: Map<String, String>? = null

    @JsonProperty("ServiceID")
    var serviceId: String? = null

    @JsonProperty("ServiceName")
    var serviceName: String? = null

    @JsonProperty("ServiceTags")
    var serviceTags: List<String>? = null

    @JsonProperty("ServiceAddress")
    var serviceAddress: String? = null

    @JsonProperty("ServiceMeta")
    var serviceMeta: Map<String, String>? = null

    @JsonProperty("ServicePort")
    var servicePort: Int? = null

    @JsonProperty("ServiceEnableTagOverride")
    var serviceEnableTagOverride: Boolean? = null

    @JsonProperty("CreateIndex")
    var createIndex: Long? = null

    @JsonProperty("ModifyIndex")
    var modifyIndex: Long? = null
    override fun toString(): String {
        return "CatalogService{" +
                "id='" + id + '\'' +
                ", node='" + node + '\'' +
                ", address='" + address + '\'' +
                ", datacenter='" + datacenter + '\'' +
                ", taggedAddresses=" + taggedAddresses +
                ", nodeMeta=" + nodeMeta +
                ", serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceTags=" + serviceTags +
                ", serviceAddress='" + serviceAddress + '\'' +
                ", serviceMeta=" + serviceMeta +
                ", servicePort=" + servicePort +
                ", serviceEnableTagOverride=" + serviceEnableTagOverride +
                ", createIndex=" + createIndex +
                ", modifyIndex=" + modifyIndex +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as CatalogService
        return id == that.id &&
                node == that.node &&
                address == that.address &&
                datacenter == that.datacenter &&
                taggedAddresses == that.taggedAddresses &&
                nodeMeta == that.nodeMeta &&
                serviceId == that.serviceId &&
                serviceName == that.serviceName &&
                serviceTags == that.serviceTags &&
                serviceAddress == that.serviceAddress &&
                serviceMeta == that.serviceMeta &&
                servicePort == that.servicePort &&
                serviceEnableTagOverride == that.serviceEnableTagOverride &&
                createIndex == that.createIndex &&
                modifyIndex == that.modifyIndex
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id,
            node,
            address,
            datacenter,
            taggedAddresses,
            nodeMeta,
            serviceId,
            serviceName,
            serviceTags,
            serviceAddress,
            serviceMeta,
            servicePort,
            serviceEnableTagOverride,
            createIndex,
            modifyIndex
        )
    }
}
