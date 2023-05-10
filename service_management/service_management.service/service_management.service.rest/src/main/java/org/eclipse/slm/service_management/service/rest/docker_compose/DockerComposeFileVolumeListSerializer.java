package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileVolume;

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
