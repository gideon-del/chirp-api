package com.gideon.chirp.service

import com.gideon.chirp.domain.events.user.UserEvent
import com.gideon.chirp.domain.exception.InvalidTokenException
import com.gideon.chirp.domain.exception.UserNotFoundException
import com.gideon.chirp.domain.models.EmailVerificationToken
import com.gideon.chirp.infra.database.entities.EmailVerificationTokenEntity
import com.gideon.chirp.infra.database.mappers.toEmailVerification
import com.gideon.chirp.infra.database.mappers.toUser
import com.gideon.chirp.infra.database.repositories.EmailVerificationTokenRepository
import com.gideon.chirp.infra.database.repositories.UserRepository
import com.gideon.chirp.infra.message_queue.EventPublisher
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private  val emailVerificationTokenRepository: EmailVerificationTokenRepository,
    private val  userRepository: UserRepository,
  @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long,
    private val eventPublisher: EventPublisher
) {

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email) ?: throw UserNotFoundException()

      emailVerificationTokenRepository.invalidateActiveTokensForUser(
            user =userEntity
        )

        val now = Instant.now()


        val token = EmailVerificationTokenEntity(
            user= userEntity,
            expiresAt = now.plus(expiryHours , ChronoUnit.HOURS),
            usedAt = null
        )

        return emailVerificationTokenRepository.save(token).toEmailVerification()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token is invalid.")

        if(verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used")
        }

        if(verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token has already expired")
        }

        emailVerificationTokenRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )
      userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            }
        ).toUser()

        eventPublisher.publish(UserEvent.Verified(
            userId = verificationToken.user.id!!,
            email = verificationToken.user.email,
            username = verificationToken.user.username
        ))
    }
    @Transactional
    fun resendVerificationEmail(email: String) {
        val token = createVerificationToken(email)

        if(token.user.hasEmailVerified){
            return
        }

        eventPublisher.publish(
            event = UserEvent.RequestResendVerification(
                userId =  token.user.id,
                email = token.user.email,
                username = token.user.username,
                verificationToken = token.token
            )
        )
    }
    @Scheduled(cron =  "0 0 3 * * *")
    fun cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteByExpiresAtLessThan(
            Instant.now()
        )
    }
}