package com.gideon.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.gideon.chirp.api.util.Password
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

data class RegisterRequest @JsonCreator constructor(
    @field:Email(message = "Must be a valid email address")
    @JsonProperty("email")
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username length must be between 3 and 20 characters")
    @JsonProperty("username")
    val username: String,
    @JsonProperty("password")
    @field:Password
    val password: String
)
