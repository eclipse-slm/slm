package org.eclipse.slm.service_management.model.offerings

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.eclipse.slm.common.model.AbstractBaseEntityUuid
import java.util.*

@Entity
@Table(name = "service_offering_git_repo")
class ServiceOfferingGitRepository(
    id: UUID? = null,
    repositoryUrl: String
) : AbstractBaseEntityUuid(id) {

    @JsonProperty
    @Column(name = "repository_url")
    var repositoryUrl: String = repositoryUrl

    @JsonProperty
    @Column(name = "git_username")
    var gitUsername = ""

    @JsonProperty
    @Column(name = "git_password")
    var gitPassword = ""

    @JsonProperty
    @Column(name = "git_tag_reg_ex")
    var gitTagRegEx = "\\d+.\\d+.\\d+"

    @Transient
    open var serviceVendorId: UUID? = null

    @OneToOne(mappedBy = "gitRepository")
    var serviceOffering: ServiceOffering? = null

    constructor() : this(null, "") {
    }
}
