package org.eclipse.slm.resource_management.features.capabilities.clusters.model

class ClusterMemberType {

    var name: String = ""

    var prettyName: String = ""

    var minNumber = 0

    var scalable: Boolean = false

    constructor() {}

    constructor(name: String, prettyName: String, minNumber: Int, scalable: Boolean)  {
        this.name = name
        this.prettyName = prettyName
        this.minNumber = minNumber
        this.scalable = scalable
    }

    override fun toString(): String {
        return "ClusterMemberType(name=$name, prettyName=$prettyName, minNumber=$minNumber, scalable=$scalable)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClusterMemberType) return false

        if (name != other.name) return false
        if (prettyName != other.prettyName) return false
        if (minNumber != other.minNumber) return false
        if (scalable != other.scalable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + prettyName.hashCode()
        result = 31 * result + minNumber
        result = 31 * result + scalable.hashCode()
        return result
    }
}
