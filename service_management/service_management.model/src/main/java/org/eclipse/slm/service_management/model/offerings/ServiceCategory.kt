package org.eclipse.slm.service_management.model.offerings

import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.eclipse.slm.common.model.AbstractBaseEntityLong

@Entity
@Table(name = "service_category")
class ServiceCategory(

        var name: String

        ) : AbstractBaseEntityLong() {

    constructor() : this("") {

    }

}
