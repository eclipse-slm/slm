package org.eclipse.slm.service_management.service.rest.docker_compose

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class DockerComposeFileVolume(
    var type: DockerComposeFileVolumeType
) {

    var target: String? = null

    var source: String? = null

    var readonly: Boolean = false

    var bind: DockerComposeFileVolumeBindOptions? = null

    var volume: DockerComposeFileVolumeOptions? = null

    var tmpfs: DockerComposeFileVolumeTmpfsOptions? = null

    @JsonCreator
    constructor() : this (DockerComposeFileVolumeType.BIND) {}

    @JsonIgnore
    constructor(volumeStringShortSyntax: String) : this() {
        if (volumeStringShortSyntax.contains(":")) {
            if (volumeStringShortSyntax.startsWith("/")
                or volumeStringShortSyntax.startsWith(".")
                or volumeStringShortSyntax.startsWith("~")) {
                this.type = DockerComposeFileVolumeType.BIND;
            }
            else {
                this.type = DockerComposeFileVolumeType.VOLUME;
            }
            this.source = volumeStringShortSyntax.split(":")[0];
            this.target = volumeStringShortSyntax.split(":")[1];
        } else {
            this.type = DockerComposeFileVolumeType.VOLUME;
            this.source = null;
            this.target = volumeStringShortSyntax;
        }

        if (volumeStringShortSyntax.contains(":ro")) {
            this.readonly = true;
        }
    }

    fun toVolumeString(): String {
        if (this.type.equals(DockerComposeFileVolumeType.BIND)
            or this.type.equals(DockerComposeFileVolumeType.VOLUME)) {

            if (this.bind == null && this.volume == null) {
                if (this.readonly) {
                    return "${this.source}:${this.target}:ro";
                }
                else {
                    if (this.source == null) {
                        return "${this.target}";
                    }
                    else {
                        return "${this.source}:${this.target}";
                    }
                }
            }
            else {
                return "Short syntax is not available when additional options 'bind' or 'volume' are set";
            }
        }
        else {
            return "Short syntax is not available for types '${DockerComposeFileVolumeType.NPIPE}' and '${DockerComposeFileVolumeType.TMPFS}'";
        }
    }
}
