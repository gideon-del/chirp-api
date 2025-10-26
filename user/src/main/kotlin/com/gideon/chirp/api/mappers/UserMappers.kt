package com.gideon.chirp.api.mappers


import com.gideon.chirp.api.dto.UserDto
import com.gideon.chirp.api.dto.AuthenticatedUserDto
import com.gideon.chirp.domain.models.AuthenticatedUser
import com.gideon.chirp.domain.models.User


fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken= accessToken,
        refreshToken = refreshToken
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasEmailVerified
    )
}