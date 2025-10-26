package com.gideon.chirp.api.dto

import com.gideon.chirp.domain.type.UserId

data class UserDto(
    val email: String,
    val username: String,
    val id: UserId,
    val hasVerifiedEmail: Boolean,
)
