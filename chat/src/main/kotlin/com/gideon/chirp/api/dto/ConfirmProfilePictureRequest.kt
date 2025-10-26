package com.gideon.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class ConfirmProfilePictureRequest @JsonCreator constructor(
    @field:NotBlank("")
    @JsonProperty("publicUrl")
    val publicUrl: String
)
