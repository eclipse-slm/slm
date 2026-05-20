package org.eclipse.slm.service_management.features.service_deployment.impl.dockercompose

class DockerComposeFileLabels() : HashMap<String, Object>()
{
    constructor(envMap: Map<String, Object>) : this() {
        this.putAll(envMap)
    }
}
