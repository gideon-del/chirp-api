package com.gideon.chirp.domain.models

data class EmailVerificationToken(
    val id: Long,
    val token: String,
    val user: User
)
