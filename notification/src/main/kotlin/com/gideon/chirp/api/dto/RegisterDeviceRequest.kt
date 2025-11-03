package com.gideon.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class RegisterDeviceRequest @JsonCreator constructor(
    @field:NotBlank
    @JsonProperty("token")
    val token: String,
    @field:NotBlank
    @JsonProperty("platform")
    val platform: PlatformDTO
)

enum class PlatformDTO{
    ANDROID,IOS
}