package org.eclipse.slm.common.model

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.util.*

@MappedSuperclass
open class StateTransition(

    @Column(nullable = false)
    var fromState: String? = null,

    @Column(nullable = false)
    var toState: String? = null,

    @Column(nullable = false)
    var timestamp: Date = Date()

) : AbstractBaseEntityLong()