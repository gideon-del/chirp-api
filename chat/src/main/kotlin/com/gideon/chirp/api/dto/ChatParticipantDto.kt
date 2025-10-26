package com.gideon.chirp.api.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.gideon.chirp.domain.type.UserId

data class ChatParticipantDto @JsonCreator constructor(
    @JsonProperty("userId")
    val userId: UserId,
    @JsonProperty("username")
    val username: String,
    @JsonProperty("email")
    val email: String,
    @JsonProperty("profilePictureUrl")
    val profilePictureUrl: String?,

)
