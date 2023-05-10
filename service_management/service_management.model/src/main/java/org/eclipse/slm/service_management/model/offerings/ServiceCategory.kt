package org.eclipse.slm.service_management.model.offerings

import org.eclipse.slm.service_management.model.AbstractBaseEntityLong
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "service_category")
class ServiceCategory(

        var name: String

        ) : AbstractBaseEntityLong() {

    constructor() : this("") {

    }

}
