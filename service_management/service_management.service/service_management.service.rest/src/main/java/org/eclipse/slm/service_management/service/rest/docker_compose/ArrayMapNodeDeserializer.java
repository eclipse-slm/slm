package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArrayMapNodeDeserializer<T extends Map<String, Object>> extends StdDeserializer<T> {

    public ArrayMapNodeDeserializer() {
        this(null);
    }

    public ArrayMapNodeDeserializer(Class<?> vc) {
        super(vc);
    }

    @SneakyThrows
    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode jsonNode = jp.getCodec().readTree(jp);

        var map = (T)this._valueClass.getDeclaredConstructor().newInstance();

        if (jsonNode.isArray()) {
            var arrayNode = (ArrayNode) jsonNode;
            for (var element : arrayNode) {
                var envVarKey = element.textValue().split("=")[0];
                var envVarValue = element.textValue().split("=")[1];
                map.put(envVarKey, envVarValue);
            }

        } else if (jsonNode.isObject()) {
            var objectNode = (ObjectNode) jsonNode;
            var iter = objectNode.fields();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                map.put(entry.getKey(), entry.getValue().asText());
            }
        }

        return map;
    }
}
