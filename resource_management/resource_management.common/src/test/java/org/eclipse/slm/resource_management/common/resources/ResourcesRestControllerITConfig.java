package org.eclipse.slm.resource_management.common.resources;

import org.eclipse.slm.resource_management.common.remote_access.CredentialUsernamePassword;

import java.util.UUID;

public class ResourcesRestControllerITConfig {

    public final static BasicResource EXISTING_RESOURCE = new BasicResource(
            UUID.randomUUID(),
            "dia-linux-881ca35a.dia.svc.fortknox.local",
            "10.3.7.142"
    );

    public final static CredentialUsernamePassword EXISTING_RESOURCE_CREDENTIAL = new CredentialUsernamePassword(
            "vfk",
            "2f16d32275"
    );

    public final static BasicResource FICTIONAL_RESOURCE = new BasicResource(
            UUID.randomUUID(),
            "SampleHost",
            "123.123.123.123"
    );

    public final static CredentialUsernamePassword FICTIONAL_RESOURCE_CREDENTIAL = new CredentialUsernamePassword(
            "sampleUser",
            "samplePassword"
    );

}
