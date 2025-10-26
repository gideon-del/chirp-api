package com.gideon.chirp.service

import com.gideon.chirp.domain.events.user.UserEvent
import com.gideon.chirp.domain.exception.EmailNotVerifiedException
import com.gideon.chirp.domain.exception.InvalidCredentialsException
import com.gideon.chirp.domain.exception.InvalidTokenException
import com.gideon.chirp.infra.database.entities.UserEntity
import com.gideon.chirp.infra.database.repositories.UserRepository
import com.gideon.chirp.domain.exception.UserAlreadyExitsException
import com.gideon.chirp.domain.exception.UserNotFoundException
import com.gideon.chirp.domain.models.AuthenticatedUser
import com.gideon.chirp.domain.models.User
import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.infra.database.entities.RefreshTokenEntity
import com.gideon.chirp.infra.database.mappers.toUser
import com.gideon.chirp.infra.database.repositories.RefreshTokenRepository
import com.gideon.chirp.infra.message_queue.EventPublisher
import com.gideon.chirp.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private  val userRepository: UserRepository,
    private  val passwordEncoder: PasswordEncoder,
    private  val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
    private val eventPublisher: EventPublisher
) {
      @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()
        val user = userRepository.findByEmailOrUsername(email=trimmedEmail,username=username.trim())


   if(user != null){
       throw UserAlreadyExitsException()
   }

        val saveUser = userRepository.saveAndFlush(
            UserEntity(
                email=trimmedEmail,
                username=username,
                hashedPassword = passwordEncoder.encode(password)
            )
        ).toUser()

        val token = emailVerificationService.createVerificationToken(trimmedEmail)

          eventPublisher.publish(
              event = UserEvent.Created(
                  userId = saveUser.id,
                  email = saveUser.email,
                  username = saveUser.username,
                  verificationToken = token.token,

              )
          )

        return saveUser
    }
    fun login(email: String, password: String): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim()) ?: throw InvalidCredentialsException()

        if(!passwordEncoder.matches(password, user.hashedPassword)) throw InvalidCredentialsException()

        if(!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return  user.id?.let {userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(userId,refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if(!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException("Invalid refresh token")
        }

        val userId = jwtService.getUserIdFromToke(refreshToken)
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)

        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId =userId,
                hashedToken = hashed) ?: throw  InvalidTokenException("Invalid refresh token")
            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId= userId,
                hashedToken = hashed
                )
            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)
            storeRefreshToken(userId, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            )
        } ?: throw UserNotFoundException()
    }
    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                hashedToken =  hashed,
                expiresAt = expiresAt
            )
        )
    }

    private  fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }
}