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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

@Service
class PushNotificationService(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val firebasePushNotificationService: FirebasePushNotificationService
){

    private val logger = LoggerFactory.getLogger(PushNotificationService::class.java)
companion object {
    private val RETRY_DELAYS_SECONDS = listOf(
        30L,
        60L,
        120L,
        300L,
        600L
    )
    const val MAX_RETRY_AGE_MINUTES = 30L
}
    private val retryQueue = ConcurrentSkipListMap<Long, MutableList<RetryData>>()
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

        sendWithRetry(
            notification =notification,
            attempt = 0
        )
    }
    fun sendWithRetry(
        notification: PushNotification,
        attempt: Int = 0
    ) {
        val result = firebasePushNotificationService.sendNotification(notification)

        result.permanentFailures.forEach {
            deviceTokenRepository.deleteByToken(it.token)
        }

        if(result.temporaryFailures.isNotEmpty() && attempt < RETRY_DELAYS_SECONDS.size) {
            val retryNotification = notification.copy(
                recipients = result.temporaryFailures
            )
            scheduleRetry(retryNotification, attempt+1)
        }


        if(result.succeeded.isNotEmpty()){
            logger.info(("Successfully sent notification to ${result.succeeded.size} devices"))
        }
    }
    fun scheduleRetry(notification: PushNotification, attempt: Int) {
        val delay = RETRY_DELAYS_SECONDS.getOrElse(attempt-1) {
            RETRY_DELAYS_SECONDS.last()
        }

        val executedAt = Instant.now().plusSeconds(delay)
        val executeAtMillis = executedAt.toEpochMilli()

        val retryData = RetryData(
            notification = notification,
            attempt = attempt,
            createdAt = Instant.now()
        )

        retryQueue.compute(executeAtMillis) {_,retries ->
            (retries ?: mutableListOf()).apply {
                add(retryData)
            }
        }

        logger.info("Scheduled retry  $attempt for ${notification.id} in $delay seconds")
    }
    @Scheduled(fixedDelay =  15_000L)
    fun processRetries() {
        val now = Instant.now()
        val nowMillis =now.toEpochMilli()

        val toProcess = retryQueue.headMap(nowMillis, true)
        if(toProcess.isEmpty()) {
            return
        }

        val entries = toProcess.entries.toList()
        entries.forEach { (timeMillis, retries) ->
            retryQueue.remove(timeMillis)

            retries.forEach { retry ->

                try {
                    val age = Duration.between(retry.createdAt, now)
                    if(age.toMinutes() > MAX_RETRY_AGE_MINUTES){
                        logger.warn("Dropping old retry (${age.toMinutes()} old)")
                        return@forEach
                    }

                    sendWithRetry(
                        notification =  retry.notification,
                        attempt = retry.attempt
                    )
                }catch (e: Exception){
                    logger.warn("Error processing retry ${retry.notification.id}")
                }
            }
        }
    }
    private data class RetryData(
        val notification: PushNotification,
        val attempt: Int,
        val createdAt: Instant
    )
}