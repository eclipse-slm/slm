package org.eclipse.slm.service_management.features.service_offerings.api.servicecategories

import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.eclipse.slm.common.model.AbstractBaseEntityLong

@Entity
@Table(name = "service_category")
class ServiceOfferingCategory(

        var name: String

        ) : AbstractBaseEntityLong() {

    constructor() : this("") {

    }

}