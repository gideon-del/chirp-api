package com.gideon.chirp.api.controller

import com.gideon.chirp.api.config.IpRateLimit
import com.gideon.chirp.api.dto.AuthenticatedUserDto
import com.gideon.chirp.api.dto.ChangePasswordRequest
import com.gideon.chirp.api.dto.EmailRequest
import com.gideon.chirp.api.dto.LoginRequest
import com.gideon.chirp.api.dto.RefreshRequest
import com.gideon.chirp.api.dto.RegisterRequest
import com.gideon.chirp.api.dto.ResetPasswordRequest
import com.gideon.chirp.api.dto.UserDto
import com.gideon.chirp.api.mappers.toAuthenticatedUserDto
import jakarta.validation.Valid
import com.gideon.chirp.api.mappers.toUserDto
import com.gideon.chirp.api.util.requestUserId
import com.gideon.chirp.infra.rate_limiting.EmailRateLimiter
import com.gideon.chirp.service.AuthService
import com.gideon.chirp.service.EmailVerificationService
import com.gideon.chirp.service.PasswordResetService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
   private val authService: AuthService,
    private  val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter
) {

    @PostMapping("/register")
    @IpRateLimit(
        request = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun registerUser(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {


     return    authService.register(
         email = body.email,
         username =  body.username,
         password = body.password).toUserDto()
    }

    @PostMapping("/login")
    @IpRateLimit(
        request = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return  authService.login(
            email =  body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }
@PostMapping("/refresh")
@IpRateLimit(
    request = 10,
    duration = 1L,
    unit = TimeUnit.HOURS
)
    fun refresh(
        @Valid @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return  authService
            .refresh(body.refreshToken)
            .toAuthenticatedUserDto()
    }
    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        request = 10,
        duration = 1L,
        unit = TimeUnit.HOURS
    )
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(body.email)
    }
   @PostMapping("/resend-verification")
   @IpRateLimit(
       request = 10,
       duration = 1L,
       unit = TimeUnit.HOURS
   )
   fun resendVerification(
       @Valid @RequestBody body: EmailRequest
   ) {
       emailRateLimiter.withRateLimit(body.email) {
emailVerificationService.resendVerificationEmail(body.email)
       }
   }
    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(body.token, body.newPassword)
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword
        )

    }
}