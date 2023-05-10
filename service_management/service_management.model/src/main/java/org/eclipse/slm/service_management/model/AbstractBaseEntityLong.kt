package org.eclipse.slm.service_management.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.eclipse.slm.service_management.model.offerings.ServiceOffering
import org.hibernate.annotations.Type
import org.springframework.data.domain.Persistable
import java.util.*
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
