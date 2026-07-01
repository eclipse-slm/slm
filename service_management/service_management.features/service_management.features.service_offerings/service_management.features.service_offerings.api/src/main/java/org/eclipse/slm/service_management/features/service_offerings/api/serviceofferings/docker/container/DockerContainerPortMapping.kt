package org.eclipse.slm.service_management.features.service_offerings.api.serviceofferings.docker.container

class DockerContainerPortMapping () {

    var hostPort: Int = -1

    var containerPort: Int = -1

    var protocol: String = ""

    constructor(hostPort: Int, containerPort: Int, protocol: String) : this() {
        this.hostPort = hostPort
        this.containerPort = containerPort
        this.protocol = protocol
    }

    constructor(hostPort: Int, containerPort: Int) : this() {
        this.hostPort = hostPort
        this.containerPort = containerPort
    }
}
