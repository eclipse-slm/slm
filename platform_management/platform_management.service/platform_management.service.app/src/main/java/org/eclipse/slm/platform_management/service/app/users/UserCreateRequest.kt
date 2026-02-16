package org.eclipse.slm.platform_management.service.app.users

import com.fasterxml.jackson.annotation.JsonProperty

data class UserCreateRequest(

    @field:JsonProperty("username")
    val username: String,

    @field:JsonProperty("firstName")
    val firstName: String,

    @field:JsonProperty("lastName")
    val lastName: String,

    @field:JsonProperty("password")
    val password: String,

    @field:JsonProperty("isPasswordTemporary")
    val isPasswordTemporary: Boolean = false,

    @field:JsonProperty("email")
    val email: String,

    )
