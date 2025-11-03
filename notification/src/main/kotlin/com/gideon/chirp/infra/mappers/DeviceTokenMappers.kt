package com.gideon.chirp.infra.mappers

import com.gideon.chirp.domain.model.DeviceToken
import com.gideon.chirp.infra.database.DeviceTokenEntity

fun DeviceTokenEntity.toDeviceToken(): DeviceToken {
    return DeviceToken(
        userId = userId,
        token =  token,
        platform = platform.toPlatform(),
        createdAt = createdAt,
        id = id
    )
}