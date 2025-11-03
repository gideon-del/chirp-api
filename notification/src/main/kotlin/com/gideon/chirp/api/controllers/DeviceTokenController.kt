package com.gideon.chirp.api.controllers

import com.gideon.chirp.api.dto.DeviceTokenDto
import com.gideon.chirp.api.dto.RegisterDeviceRequest
import com.gideon.chirp.api.mappers.toDeviceTokenDto
import com.gideon.chirp.api.mappers.toPlatform
import com.gideon.chirp.api.util.requestUserId
import com.gideon.chirp.service.PushNotificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notification")
class DeviceTokenController(
    private val pushNotificationService: PushNotificationService
) {

    @PostMapping("/register")
    fun registerDeviceToken(
        @Valid @RequestBody body: RegisterDeviceRequest
    ): DeviceTokenDto {
        return pushNotificationService.registerDevice(
            userId = requestUserId,
            token = body.token,
            platform = body.platform.toPlatform()
        ).toDeviceTokenDto()
    }

    @DeleteMapping("/{token}")
    fun unregisterDeviceToken(
        @RequestParam token: String
    ) {
        pushNotificationService.unregisterToken(token)
    }
}