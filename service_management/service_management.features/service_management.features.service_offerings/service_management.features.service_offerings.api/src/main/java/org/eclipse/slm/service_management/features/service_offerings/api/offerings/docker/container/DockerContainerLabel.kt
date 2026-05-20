package org.eclipse.slm.service_management.features.service_offerings.api.offerings.docker.container

class DockerContainerLabel () {

    var name: String = ""

    var value: Any? = null

    constructor(name: String, value: Any?) : this() {
        this.name = name
        this.value = value
    }
}
