package com.gideon.chirp.infra.database.mappers

import com.gideon.chirp.infra.database.entities.UserEntity
import com.gideon.chirp.domain.models.User

fun UserEntity.toUser(): User {
    return User(
        id= id!!,
        username = username,
        email = email,
        hasEmailVerified = hasVerifiedEmail
    )
}