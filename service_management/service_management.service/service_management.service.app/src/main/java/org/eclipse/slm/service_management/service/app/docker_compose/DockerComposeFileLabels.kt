package org.eclipse.slm.service_management.service.app.docker_compose

class DockerComposeFileLabels() : HashMap<String, Object>()
{
    constructor(envMap: Map<String, Object>) : this() {
        this.putAll(envMap);
    }
}
