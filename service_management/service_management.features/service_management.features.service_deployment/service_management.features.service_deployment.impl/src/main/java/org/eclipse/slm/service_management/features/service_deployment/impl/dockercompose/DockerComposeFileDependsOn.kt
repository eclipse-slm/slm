package org.eclipse.slm.service_management.features.service_deployment.impl.dockercompose

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonSerialize(using = DockerComposeFileDependsOnSerializer::class)
@JsonDeserialize(contentUsing = DockerComposeFileDependsOnDeserializer::class)
class DockerComposeFileDependsOn() : HashMap<String, DockerComposeFileDependsOnDefinition>()
{
}
