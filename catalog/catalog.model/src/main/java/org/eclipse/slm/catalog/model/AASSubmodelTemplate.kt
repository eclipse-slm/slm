package org.eclipse.slm.catalog.model

import jakarta.persistence.Entity

@Entity(name = "aas_submodel_templates")
class AASSubmodelTemplate (

    val semanticId: String,

    val name: String

) : AbstractBaseEntityLong() {

    constructor() : this("", "") {}

}
