package org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose

class DockerComposeFileLabels() : HashMap<String, Object>()
{
    constructor(envMap: Map<String, Object>) : this() {
        this.putAll(envMap)
    }
}
