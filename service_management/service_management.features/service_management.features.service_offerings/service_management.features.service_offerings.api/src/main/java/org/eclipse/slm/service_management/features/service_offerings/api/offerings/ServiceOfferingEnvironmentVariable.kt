package org.eclipse.slm.service_management.features.service_offerings.api.offerings

class ServiceOfferingEnvironmentVariable () {

    var key: String = ""

    var value: Any? = null

    constructor(key: String, value: Any?) : this() {
        this.key = key
        this.value = value
    }
}
