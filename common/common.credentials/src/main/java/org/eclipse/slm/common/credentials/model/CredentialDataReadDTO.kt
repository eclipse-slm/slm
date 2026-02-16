package org.eclipse.slm.common.credentials.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "credentialDataType")
@JsonSubTypes(
    JsonSubTypes.Type(value = CredentialDataUsernamePasswordReadDTO::class, name = "USERNAME_PASSWORD"),
    JsonSubTypes.Type(value = CredentialDataKeyPairReadDTO::class, name = "KEY_PAIR")
)
abstract class CredentialDataReadDTO (

    val credentialDataType: CredentialDataType

)