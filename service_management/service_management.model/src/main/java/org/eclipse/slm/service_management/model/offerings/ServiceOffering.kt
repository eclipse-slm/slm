package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import jakarta.persistence.*
import org.eclipse.slm.common.model.AbstractBaseEntityUuid
import org.eclipse.slm.service_management.model.utils.ByteArrayDeserializer
import org.eclipse.slm.service_management.model.vendors.ServiceVendor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.Transient

@Entity
@Table(name = "service_offering")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Transactional
open class ServiceOffering(id: UUID? = null) : AbstractBaseEntityUuid(id) {

    @Transient
    private val LOG: Logger = LoggerFactory.getLogger(ServiceOffering::class.java)

    @Column(name = "name")
    open var name = ""

    @Column(name = "description", columnDefinition = "LONGTEXT")
    open var description = ""

    @Column(name = "short_description", columnDefinition = "LONGTEXT")
    open var shortDescription = ""

    @Column(name = "cover_image", columnDefinition="MEDIUMBLOB")
    @JsonDeserialize(using = ByteArrayDeserializer::class)
    open var coverImage: ByteArray? = null

    @ManyToOne(cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name="service_category_id", nullable = false)
    open var serviceCategory : ServiceCategory? = null

    @ManyToOne(cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name="service_vendor_id", nullable = false)
    open var serviceVendor: ServiceVendor? = null

    @OneToMany(mappedBy = "serviceOffering", cascade = [ CascadeType.ALL ], orphanRemoval = true)
    @Column(name = "versions")
    open var versions: MutableList<ServiceOfferingVersion> = mutableListOf()

    @OneToOne(cascade = [ CascadeType.ALL ] )
    @JoinColumn(name = "git_repository_id", referencedColumnName = "id")
    open var gitRepository: ServiceOfferingGitRepository? = null

    override fun toString(): String {
        return "[id=${this.id} name ='${this.name}']"
    }

    fun hasVersionWithId(serviceOfferingVersionId: UUID): Optional<ServiceOfferingVersion> {
        return versions.stream()
            .filter { version: ServiceOfferingVersion -> version.id == serviceOfferingVersionId }
            .findAny();
    }

}
