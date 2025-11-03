package com.gideon.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.gideon.chirp.domain.type.UserId
import java.time.Instant

data class DeviceTokenDto @JsonCreator constructor(
    @JsonProperty("userId")
    val userId: UserId,
    @JsonProperty("token")
    val token: String,
    @JsonProperty("createdAt")
    val createdAt: Instant
)
