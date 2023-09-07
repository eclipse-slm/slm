package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class DockerComposeFileDependsOnSerializer extends JsonSerializer<DockerComposeFileDependsOn> {

    @Override
    public void serialize(DockerComposeFileDependsOn value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {


        if (value.values().stream().findFirst().get().getCondition() == null) {
            jgen.writeStartArray();
            for (var dependsOnEntry : value.entrySet()) {
                jgen.writeString(dependsOnEntry.getValue().getServiceName());
            }
            jgen.writeEndArray();
        }
        else {
            jgen.writeStartObject();
            for (var dependsOnEntry : value.entrySet()) {
                jgen.writeObjectFieldStart(dependsOnEntry.getValue().getServiceName());
                jgen.writeStringField("condition", dependsOnEntry.getValue().getCondition().getValue());
                jgen.writeEndObject();

            }
            jgen.writeEndObject();
        }
    }

}
