package com.gideon.chirp.api.mappers

import com.gideon.chirp.api.dto.DeviceTokenDto
import com.gideon.chirp.api.dto.PlatformDTO
import com.gideon.chirp.domain.model.DeviceToken


fun DeviceToken.toDeviceTokenDto(): DeviceTokenDto {
    return DeviceTokenDto(
        token=token,
        userId = userId,
        createdAt = createdAt
    )
}

fun  PlatformDTO.toPlatform(): DeviceToken.Platform {
    return  when(this) {
       PlatformDTO.ANDROID -> DeviceToken.Platform.ANDROID
        PlatformDTO.IOS -> DeviceToken.Platform.IOS
    }
}