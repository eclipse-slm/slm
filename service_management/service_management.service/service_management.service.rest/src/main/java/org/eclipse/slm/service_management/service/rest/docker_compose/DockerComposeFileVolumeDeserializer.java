package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileVolume;

import java.io.IOException;

public class DockerComposeFileVolumeDeserializer extends StdDeserializer<DockerComposeFileVolume> {

    public DockerComposeFileVolumeDeserializer() {
        this(null);
    }

    public DockerComposeFileVolumeDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DockerComposeFileVolume deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        if (node instanceof ObjectNode) {
            var mapper = new ObjectMapper(new YAMLFactory());
            mapper.registerModule(new KotlinModule());
            return mapper.readValue(node.traverse(), DockerComposeFileVolume.class);
        }
        else {
            var volumeString = node.textValue();
            return new DockerComposeFileVolume(volumeString);
        }
    }
}
