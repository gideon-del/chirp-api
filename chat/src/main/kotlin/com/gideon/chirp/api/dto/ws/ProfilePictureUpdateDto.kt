package com.gideon.chirp.api.dto.ws

import com.gideon.chirp.domain.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?
)
