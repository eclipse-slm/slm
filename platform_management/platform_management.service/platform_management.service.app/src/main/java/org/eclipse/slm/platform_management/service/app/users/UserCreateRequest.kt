package org.eclipse.slm.platform_management.service.app.users

import com.fasterxml.jackson.annotation.JsonProperty

data class UserCreateRequest(

    @param:JsonProperty("username")
    val username: String,

    @param:JsonProperty("firstName")
    val firstName: String,

    @param:JsonProperty("lastName")
    val lastName: String,

    @param:JsonProperty("password")
    val password: String,

    @param:JsonProperty("isPasswordTemporary")
    val isPasswordTemporary: Boolean = false,

    @param:JsonProperty("email")
    val email: String,

    @param:JsonProperty("isAdmin")
    val isAdmin: Boolean = false,

)
