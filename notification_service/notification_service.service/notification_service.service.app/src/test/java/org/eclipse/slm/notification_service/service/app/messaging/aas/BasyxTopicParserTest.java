package org.eclipse.slm.notification_service.service.app.messaging.aas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.slm.notification_service.service.app.messaging.aas.BasyxTopicParser.parse;

class BasyxTopicParserTest {

    private static String b64(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(UTF_8));
    }

    @Test
    void shellLifecycle() {
        assertThat(parse("aas-repository/repo1/shells/updated"))
                .contains(new BasyxTopic(AasElementKind.SHELL, AasEventType.UPDATED, null, null));
    }

    @Test
    void submodelLifecycle() {
        assertThat(parse("sm-repository/r/submodels/deleted"))
                .contains(new BasyxTopic(AasElementKind.SUBMODEL, AasEventType.DELETED, null, null));
    }

    @Test
    void repositoryReferenceShape() {
        assertThat(parse("aas-repository/r/shells/submodels/" + b64("sm-1") + "/created"))
                .contains(new BasyxTopic(AasElementKind.SUBMODEL_REFERENCE, AasEventType.CREATED, null, "sm-1"));
    }

    @Test
    void serviceReferenceShape() {
        assertThat(parse("aas-repository/r/shells/" + b64("Resource_abc") + "/submodelReferences/deleted"))
                .contains(new BasyxTopic(AasElementKind.SUBMODEL_REFERENCE, AasEventType.DELETED, "Resource_abc", null));
    }

    @Test
    void decodesUnpaddedUrlSafeIds() {
        var id = "https://example.com/ids/sm/1234_ä?x=1";
        assertThat(parse("aas-repository/r/shells/submodels/" + b64(id) + "/created")).get()
                .extracting(BasyxTopic::submodelId).isEqualTo(id);
    }

    @Test
    void keepsRawIdOfBasyxVersionsWithoutTopicEncoding() {
        // Valid Base64URL characters, but decoding yields no UTF-8 text
        var id = "Nameplate-aa9fd3af-defb-44e2-921b-4ac05a16c577";
        assertThat(parse("aas-repository/r/shells/submodels/" + id + "/created")).get()
                .extracting(BasyxTopic::submodelId).isEqualTo(id);
    }

    @Test
    void keepsRawIdContainingSlashes() {
        var id = "https://example.com/ids/sm/1";
        assertThat(parse("aas-repository/r/shells/submodels/" + id + "/deleted"))
                .contains(new BasyxTopic(AasElementKind.SUBMODEL_REFERENCE, AasEventType.DELETED, null, id));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "aas-repository/r/shells/patched",
            "aas-repository/r/shells/submodels/c20tMQ/updated",
            "sm-repository/r/submodels/x/submodelElements/a/updated",
            "concept-description-repository/r/conceptDescriptions/created",
            "aas-repository/r/shells",
            "aas-repository/r/shells/%%%/submodelReferences/created",
            ""
    })
    void unsupportedTopicsAreEmpty(String topic) {
        assertThat(parse(topic)).isEmpty();
    }
}
