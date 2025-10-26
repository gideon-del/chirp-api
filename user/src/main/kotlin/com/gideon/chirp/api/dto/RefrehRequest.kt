package com.gideon.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class RefreshRequest @JsonCreator constructor(
    @field:NotBlank("Refresh Token is required")
    @JsonProperty("refreshToken") val refreshToken: String
)
