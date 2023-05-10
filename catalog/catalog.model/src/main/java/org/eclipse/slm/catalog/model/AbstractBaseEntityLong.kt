package org.eclipse.slm.catalog.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

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
