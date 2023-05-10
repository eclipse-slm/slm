package org.eclipse.slm.service_management.model.offerings.docker.container

class DockerContainerVolume () {

    var name: String = "";

    var containerPath: String = ""

    constructor(name: String, containerPath: String) : this() {
        this.name = name
        this.containerPath = containerPath
    }
}
