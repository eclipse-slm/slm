package org.eclipse.slm.notification_service.service.app.messaging.aas;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;

/**
 * Parses the lifecycle topics published by the BaSyx AAS repository and submodel repository:
 * <ul>
 *     <li>{@code aas-repository/{repo}/shells/{created|updated|deleted}}</li>
 *     <li>{@code aas-repository/{repo}/shells/submodels/{smIdB64}/{created|deleted}}</li>
 *     <li>{@code aas-repository/{repo}/shells/{shellIdB64}/submodelReferences/{created|deleted}}</li>
 *     <li>{@code sm-repository/{repo}/submodels/{created|updated|deleted}}</li>
 * </ul>
 */
public final class BasyxTopicParser {

    private static final Set<AasEventType> LIFECYCLE_EVENTS = Set.of(AasEventType.CREATED, AasEventType.UPDATED, AasEventType.DELETED);

    private static final Set<AasEventType> REFERENCE_EVENTS = Set.of(AasEventType.CREATED, AasEventType.DELETED);

    private BasyxTopicParser() {
    }

    public static Optional<BasyxTopic> parse(String topic) {
        var segments = topic.split("/");
        try {
            if (segments.length == 4 && segments[0].equals("aas-repository") && segments[2].equals("shells")) {
                return eventType(segments[3], LIFECYCLE_EVENTS)
                        .map(eventType -> new BasyxTopic(AasElementKind.SHELL, eventType, null, null));
            }
            if (segments.length == 4 && segments[0].equals("sm-repository") && segments[2].equals("submodels")) {
                return eventType(segments[3], LIFECYCLE_EVENTS)
                        .map(eventType -> new BasyxTopic(AasElementKind.SUBMODEL, eventType, null, null));
            }
            if (segments.length == 6 && segments[0].equals("aas-repository") && segments[2].equals("shells")) {
                if (segments[4].equals("submodelReferences")) {
                    var shellId = decode(segments[3]);
                    return eventType(segments[5], REFERENCE_EVENTS)
                            .map(eventType -> new BasyxTopic(AasElementKind.SUBMODEL_REFERENCE, eventType, shellId, null));
                }
                if (segments[3].equals("submodels")) {
                    var submodelId = decode(segments[4]);
                    return eventType(segments[5], REFERENCE_EVENTS)
                            .map(eventType -> new BasyxTopic(AasElementKind.SUBMODEL_REFERENCE, eventType, null, submodelId));
                }
            }
        } catch (IllegalArgumentException e) {
            // Identifier in topic is not valid Base64URL
        }
        return Optional.empty();
    }

    private static Optional<AasEventType> eventType(String segment, Set<AasEventType> allowed) {
        return allowed.stream().filter(eventType -> eventType.name().equalsIgnoreCase(segment)).findFirst();
    }

    private static String decode(String base64UrlId) {
        return new String(Base64.getUrlDecoder().decode(base64UrlId), StandardCharsets.UTF_8);
    }
}
