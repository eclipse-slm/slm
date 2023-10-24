package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class DockerComposeFileDependsOnDeserializer extends StdDeserializer<DockerComposeFileDependsOn> {

    public DockerComposeFileDependsOnDeserializer() {
        this(null);
    }

    public DockerComposeFileDependsOnDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public DockerComposeFileDependsOn deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        var dockerComposeFileDependsOn = new DockerComposeFileDependsOn();

        if (node instanceof ObjectNode) {
            Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                var serviceName = entry.getKey();
                var conditionValue = entry.getValue().get("condition").textValue();
                var condition = DockerComposeFileDependsOnConditionType.valueOf(conditionValue.toUpperCase());
                dockerComposeFileDependsOn.put(serviceName, new DockerComposeFileDependsOnDefinition(serviceName, condition));
            }
        }
        else if (node instanceof ArrayNode){
            for (JsonNode arrayNode : node) {
                var serviceName = arrayNode.asText();
                dockerComposeFileDependsOn.put(serviceName, new DockerComposeFileDependsOnDefinition(serviceName));
            }
        }

        return dockerComposeFileDependsOn;
    }
}
