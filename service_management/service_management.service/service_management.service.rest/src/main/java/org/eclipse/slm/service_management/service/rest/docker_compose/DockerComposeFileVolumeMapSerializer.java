package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class DockerComposeFileVolumeMapSerializer extends JsonSerializer<Map<String, Object>> {

    @Override
    public void serialize(Map<String, Object> value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        for (var volumeDefinition : value.entrySet())
        {
            if (volumeDefinition.getValue() == null)
            {
                jgen.writeObjectFieldStart(volumeDefinition.getKey());
                jgen.writeStringField("driver", "local");
                jgen.writeEndObject();

            }
            else
            {
                jgen.writeObjectField(volumeDefinition.getKey(), volumeDefinition.getValue());
            }
        }
        jgen.writeEndObject();
    }

}
