package com.gideon.chirp.domain.models

import com.gideon.chirp.domain.type.UserId
import java.util.UUID


data class User(
    val id: UserId,
    val email: String,
    val hasEmailVerified: Boolean,
    val username: String
)
