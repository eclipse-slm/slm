package org.eclipse.slm.service_management.service.app.docker_compose

class DockerComposeFileDependsOnDefinition(val serviceName: String)
{
    constructor(serviceName: String, condition: DockerComposeFileDependsOnConditionType) : this(serviceName) {
        this.condition = condition
    }

    var condition: DockerComposeFileDependsOnConditionType? = null
}
