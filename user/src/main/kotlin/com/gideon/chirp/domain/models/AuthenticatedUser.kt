package com.gideon.chirp.domain.models

data class AuthenticatedUser(
    val user: User,
    val accessToken: String,
    val refreshToken: String,

)
