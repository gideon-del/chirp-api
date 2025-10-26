package com.gideon.chirp.domain.events.user

import com.gideon.chirp.domain.events.ChirpEvent
import com.gideon.chirp.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed  class UserEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now()
): ChirpEvent {

    data class Created(
        val email: String,
        val userId: UserId,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_CREATED_KEY
    ): UserEvent(), ChirpEvent

    data class Verified(
        val email: String,
        val userId: UserId,
        val username: String,
        override val eventKey: String = UserEventConstants.USER_VERIFIED
    ): UserEvent(), ChirpEvent

    data class RequestResendVerification(
        val email: String,
        val userId: UserId,
        val username: String,
        val verificationToken: String,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESEND_VERIFICATION
    ): UserEvent(), ChirpEvent

    data class RequestResetPassword(
        val email: String,
        val userId: UserId,
        val username: String,
        val verificationToken: String,
        val expireInMinuets: Long,
        override val eventKey: String = UserEventConstants.USER_REQUEST_RESET_PASSWORD
    ): UserEvent(), ChirpEvent
}