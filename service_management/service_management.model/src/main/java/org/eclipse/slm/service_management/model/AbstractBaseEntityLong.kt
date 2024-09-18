package org.eclipse.slm.service_management.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@MappedSuperclass
abstract class AbstractBaseEntityLong {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id", unique = true, nullable = false)
    val id: Long = 0

    @Version
    @JsonIgnore
    open val entityVersion: Long? = 0
}
