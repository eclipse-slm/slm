package org.eclipse.slm.resource_management.common.remote_access

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "credentialClass"
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = CredentialUsernamePassword::class,
        name = "CredentialUsernamePassword"
    )
)
abstract class Credential()
