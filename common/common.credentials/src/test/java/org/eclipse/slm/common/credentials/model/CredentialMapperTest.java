package org.eclipse.slm.common.credentials.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialMapperTest {

    private final CredentialMapper mapper = CredentialMapper.INSTANCE;

    private final List<String> TEST_CREDENTIAL_SCOPES = List.of("TEST_SCOPE");

    @Test
    void mapsUsernamePasswordCredentialToDto() {
        var id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var name = "credname";

        var source = new Credential(id, name, TEST_CREDENTIAL_SCOPES, new CredentialDataUsernamePassword("alice", "secret"));

        var result = mapper.toReadDTO(source);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getScopesRaw()).isEqualTo(TEST_CREDENTIAL_SCOPES);
        assertThat(result.getData()).isInstanceOf(CredentialDataUsernamePasswordReadDTO.class)
                .satisfies(d -> {
                    var cast = (CredentialDataUsernamePasswordReadDTO) d;
                    assertThat(cast.getUsername()).isEqualTo("alice");
                });
    }

    @Test
    void mapsKeyPairCredentialToDto() {
        var id = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var name = "credname";

        var source = new Credential(id, name, TEST_CREDENTIAL_SCOPES, new CredentialDataKeyPair("priv-key", "pub-key"));

        var result = mapper.toReadDTO(source);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getScopesRaw()).isEqualTo(TEST_CREDENTIAL_SCOPES);
        assertThat(result.getData()).isInstanceOf(CredentialDataKeyPairReadDTO.class)
                .satisfies(d -> {
                    var cast = (CredentialDataKeyPairReadDTO) d;
                    assertThat(cast.getPublicKey()).isEqualTo("pub-key");
                });
    }
}
