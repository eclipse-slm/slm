package org.eclipse.slm.common.consul.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.eclipse.slm.common.consul.model.catalog.Node;

import java.io.IOException;

public class NodeSerializer extends StdSerializer<Node> {

    public NodeSerializer() {
        this(null);
    }

    public NodeSerializer(Class<Node> t) {
        super(t);
    }

    @Override
    public void serialize(Node value, JsonGenerator jgen,
                          SerializerProvider provider) throws IOException,
            JsonProcessingException {

        jgen.writeStartObject();

        provider.defaultSerializeField("ID", value.getId(), jgen);
        provider.defaultSerializeField("Node", value.getNode(), jgen);
        provider.defaultSerializeField("Address", value.getAddress(), jgen);
        provider.defaultSerializeField("Datacenter", value.getDatacenter(), jgen);
        provider.defaultSerializeField("TaggedAddresses", value.getTaggedAddresses(), jgen);
        provider.defaultSerializeField("NodeMeta", value.getMeta(), jgen);
        provider.defaultSerializeField("CreateIndex", value.getCreateIndex(), jgen);
        provider.defaultSerializeField("ModifyIndex", value.getModifyIndex(), jgen);

        jgen.writeEndObject();

    }

}

