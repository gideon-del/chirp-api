package com.gideon.chirp.service

import com.gideon.chirp.domain.exception.InvalidDeviceTokenException
import com.gideon.chirp.domain.model.DeviceToken
import com.gideon.chirp.domain.model.PushNotification
import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.infra.database.DeviceTokenEntity
import com.gideon.chirp.infra.database.DeviceTokenRepository
import com.gideon.chirp.infra.mappers.toDeviceToken
import com.gideon.chirp.infra.mappers.toPlatformEntity
import com.gideon.chirp.infra.push_notification.FirebasePushNotificationService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
){

    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)

    @Transactional
    fun registerDevice(userId: UserId, token: String, platform: DeviceToken.Platform): DeviceToken {
        val existing = deviceTokenRepository.findByToken(token)
        val trimmedToken = token.trim()

        if(existing === null && !firebasePushNotificationService.isValidToken(trimmedToken)){
throw InvalidDeviceTokenException()
        }

        val entity = if(existing != null){
            deviceTokenRepository.save(
                existing.apply {
                    this.userId === userId
                }
            )
        }else {
            deviceTokenRepository.save(
                DeviceTokenEntity(
                    userId = userId,
                    token = token,
platform = platform.toPlatformEntity()
                )
            )
        }
        return entity.toDeviceToken()
    }

    @Transactional
    fun unregisterToken(token: String) {
        deviceTokenRepository.deleteByToken(token.trim())
    }

    fun sendNewMessageNotification(
        recipientUserIds: List<UserId>,
        senderUserId: UserId,
        senderUsername: String,
        message: String,
        chatId: ChatId
    ) {
        val devicesTokens = deviceTokenRepository.findByUserIdIn(recipientUserIds)

        if(devicesTokens.isEmpty()){
            logger.warn("No push notification token found")
            return
        }

        val recipients = devicesTokens
            .filter { it.userId != senderUserId }
            .map { it.toDeviceToken() }

        val notification = PushNotification(
            message=message,
            title = "New message from $senderUsername",
            recipients = recipients,
            chatId = chatId,
            data = mapOf(
                "chatId" to chatId.toString(),
                "type" to "new_message"
            )
        )

        firebasePushNotificationService.sendNotification(notification)
    }
}