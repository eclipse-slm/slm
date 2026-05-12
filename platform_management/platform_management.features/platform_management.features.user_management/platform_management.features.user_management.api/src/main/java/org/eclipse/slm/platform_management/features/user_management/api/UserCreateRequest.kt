package org.eclipse.slm.platform_management.features.user_management.api

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class UserCreateRequest(

    @field:NotBlank(message = "Username must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-z][A-Za-z0-9_-]*$",
        message = "Username must start with a letter and contain only letters, numbers, '-' and '_'",
    )
    @param:JsonProperty("username")
    val username: String,

    @field:NotBlank(message = "First name must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-z][A-Za-z .-]*$",
        message = "First name must start with a letter and may only contain letters, spaces, '.' and '-'",
    )
    @param:JsonProperty("firstName")
    val firstName: String,

    @field:NotBlank(message = "Last name must not be blank")
    @field:Pattern(
        regexp = "^[A-Za-z][A-Za-z.-]*$",
        message = "Last name must start with a letter and may only contain letters, '.' and '-'",
    )
    @param:JsonProperty("lastName")
    val lastName: String,

    @param:JsonProperty("password")
    val password: String,

    @param:JsonProperty("isPasswordTemporary")
    val isPasswordTemporary: Boolean = false,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be a valid email address")
    @param:JsonProperty("email")
    val email: String,

    @param:JsonProperty("isAdmin")
    val isAdmin: Boolean = false,
)

