package org.eclipse.slm.resource_management.service.discovery.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.uuid.Generators;
import org.eclipse.slm.resource_management.model.discovery.DiscoveredResource;
import org.eclipse.slm.resource_management.model.discovery.DriverInfo;
import org.eclipse.slm.resource_management.model.discovery.exceptions.DiscoveryResponseParsingFailed;
import siemens.common.identifiers.v1.CommonIdentifiers;

public class DataFormatUtil {

    public final static String CLASSIFIER_PREFIX = "https://schema.industrial-assets.io/base/v\\d+.\\d+.\\d+/Asset#";

    public static DiscoveredResource convertFromJsonToDiscoveredResource(DriverInfo driverInfo, JsonNode jsonNode) throws DiscoveryResponseParsingFailed {

        var discoveredResourceBuilder = new DiscoveredResource.Builder();

        var id = "";
        if (jsonNode.has("product_instance_identifier")) {
            if (jsonNode.get("product_instance_identifier").has("manufacturer_product")) {
                if (jsonNode.get("product_instance_identifier").get("manufacturer_product").has("id")) {
                    id = jsonNode.get("product_instance_identifier").get("manufacturer_product").get("id").asText();
                    discoveredResourceBuilder.id(id);
                }
            }
        }
        if (jsonNode.has("id_link")) {
            id = jsonNode.get("id_link").asText();
            discoveredResourceBuilder.id(id);
        }

        if (id.isEmpty()) {
            throw new DiscoveryResponseParsingFailed(jsonNode, "Failed to find property for asset id");
        }

        if (jsonNode.has("name")) {
            var name = jsonNode.get("name").get("name").asText();
            discoveredResourceBuilder.name(name);
        }
        if (!jsonNode.has("name") && jsonNode.has("@type")) {
            var name = jsonNode.get("@type").get("@type").asText();
            discoveredResourceBuilder.name(name);
        }
        var manufacturer = jsonNode.get("product_instance_identifier").get("manufacturer_product").get("manufacturer").get("name").asText();
        var productId = jsonNode.get("product_instance_identifier").get("manufacturer_product").get("product_id").asText();
        var serialNumber = jsonNode.get("product_instance_identifier").get("serial_number").asText();
        var ipAddress = jsonNode.get("connection_points").get("ipv4_address").asText();
        if (jsonNode.has("mac_address")) {
            var macAddress = jsonNode.get("mac_address").asText();
            discoveredResourceBuilder.macAddress(macAddress);
        }
        if (jsonNode.has("mac_identifiers")) {
            var macAddress = jsonNode.get("mac_identifiers").get("mac_address").asText();
            discoveredResourceBuilder.macAddress(macAddress);
        }
        if (jsonNode.has("software_components")) {
            var fieldsIterator = jsonNode.get("software_components").fields();
            while (fieldsIterator.hasNext()) {
                var field = fieldsIterator.next();
                if (field.getKey().toLowerCase().contains("firmware")) {
                    var firmwareVersion = jsonNode.get("software_components").get(field.getKey()).asText();
                    discoveredResourceBuilder.firmwareVersion(firmwareVersion);
                }
            }
        }

        var uuidNamespace = Generators.nameBasedGenerator().generate(driverInfo.getInstanceId());
        var resourceId = Generators.nameBasedGenerator(uuidNamespace).generate(id);

        var discoveredResource = discoveredResourceBuilder
                .resourceId(resourceId)
                .serialNumber(serialNumber)
                .manufacturerName(manufacturer)
                .productName(productId)
                .ipAddress(ipAddress)
            .build();

        return discoveredResource;
    }

    public static JsonNode convertToJson(CommonIdentifiers.DeviceIdentifier deviceIdentifier) {
        var classifier = deviceIdentifier.getClassifiers(0);
        var classifierValue = classifier.getValue();
        var jsonPropName = classifierValue.replaceAll(CLASSIFIER_PREFIX, "");

        var objectMapper = new ObjectMapper();
        var json = objectMapper.createObjectNode();

        if (deviceIdentifier.hasChildren()) {
            var children = deviceIdentifier.getChildren().getValueList();

            var childJson = objectMapper.createObjectNode();


            for (var childDeviceIdentifier : children ) {
                var childName = childDeviceIdentifier.getClassifiers(0).getValue().replaceAll(CLASSIFIER_PREFIX, "");
                childName = childName.replace(jsonPropName + "/", "");

                if (childDeviceIdentifier.hasChildren()) {
                    var childJsonObject = DataFormatUtil.convertToJson(childDeviceIdentifier);
                    childJson.put(childName, childJsonObject);
                }
                else {
                    if (childName.contains("/")) {
                        var resolvedNode = resolvePropertyNameSegments(childName, childDeviceIdentifier.getText());
                        JsonUtil.merge(childJson, resolvedNode);
                    }
                    else {
                        childJson.put(childName, childDeviceIdentifier.getText());
                    }
                }
            }

            return childJson;
        }
        else {
            if (jsonPropName.contains("/")) {
                var resolvedNode = resolvePropertyNameSegments(jsonPropName, deviceIdentifier.getText());
                JsonUtil.merge(json, resolvedNode);
            }
            else {
                json.put(jsonPropName, deviceIdentifier.getText());
            }
            return json;
        }
    }

    public static ObjectNode resolvePropertyNameSegments(String propertyName, String value) {
        var objectMapper = new ObjectMapper();

        if (propertyName.contains("/")) {
            var jsonPropNameSegments = propertyName.split("/");

            var remainingSegments = propertyName.replace(jsonPropNameSegments[0] + "/", "");

            var childSegmentNode = resolvePropertyNameSegments(remainingSegments, value);

            var node = objectMapper.createObjectNode();
            node.put(jsonPropNameSegments[0], childSegmentNode);

            return node;
        }
        else {
            var node = objectMapper.createObjectNode();
            node.put(propertyName, value);

            return node;
        }
    }



}
