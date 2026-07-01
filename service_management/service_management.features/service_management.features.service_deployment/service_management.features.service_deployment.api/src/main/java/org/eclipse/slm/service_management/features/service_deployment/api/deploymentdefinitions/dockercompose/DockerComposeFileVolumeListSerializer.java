package org.eclipse.slm.service_management.features.service_deployment.api.deploymentdefinitions.dockercompose;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class DockerComposeFileVolumeListSerializer extends JsonSerializer<List<DockerComposeFileVolume>> {

    @Override
    public void serialize(List<DockerComposeFileVolume> value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {
        jgen.writeStartArray();
        for (var volume : value)
        {
            jgen.writeString(volume.toVolumeString());
        }
        jgen.writeEndArray();
    }

}

