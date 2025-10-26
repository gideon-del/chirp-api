package com.gideon.chirp.infra.message_queue

import com.gideon.chirp.domain.events.user.UserEvent
import com.gideon.chirp.service.EmailService
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Component
class NotificationUserEventListener(
    private val emailService: EmailService
) {

    @RabbitListener(queues = [MessageQueues.NOTIFICATION_USER_EVENTS])
    fun handleUserEvent(event: UserEvent){
        when(event) {
            is UserEvent.Created -> {
               emailService.sendVerificationEmail(
                   email = event.email,
                   username = event.username,
                   token = event.verificationToken,
                   userId = event.userId
               )
            }
            is UserEvent.RequestResendVerification -> {
                emailService.sendVerificationEmail(
                    email = event.email,
                    username = event.username,
                    token = event.verificationToken,
                    userId = event.userId
                )
            }
            is UserEvent.RequestResetPassword -> {
                emailService.sendPasswordResetEmail(
                    email = event.email,
                    username = event.username,
                    token = event.verificationToken,
                    userId = event.userId,
                    expiration = Duration.ofMinutes(event.expireInMinuets)
                )
            }
            else -> Unit
        }
    }
}