package com.gideon.chirp.api.exception_handling

import com.gideon.chirp.domain.exception.EmailNotVerifiedException
import com.gideon.chirp.domain.exception.InvalidCredentialsException
import com.gideon.chirp.domain.exception.InvalidTokenException
import com.gideon.chirp.domain.exception.RateLimitException
import com.gideon.chirp.domain.exception.SamePasswordException
import com.gideon.chirp.domain.exception.UnauthorizedException
import com.gideon.chirp.domain.exception.UserAlreadyExitsException
import com.gideon.chirp.domain.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.web.csrf.InvalidCsrfTokenException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {
   @ExceptionHandler(UserAlreadyExitsException::class)
   @ResponseStatus(HttpStatus.CONFLICT)
    open fun onUserAlreadyExists(e: UserAlreadyExitsException) = mapOf(
        "code" to "USER_EXISTS",
        "message" to e.message
    )
    @ExceptionHandler(InvalidTokenException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    open fun onInvalidToken(e: InvalidTokenException) = mapOf(
        "code" to "INVALID_TOKEN",
        "message" to e.message
    )

    @ExceptionHandler(InvalidCredentialsException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    open fun onInvalidCredentials(e: InvalidCredentialsException) = mapOf(
        "code" to "INVALID_CREDENTIALS",
        "message" to e.message
    )
    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    open fun onUnauthorized(e: UnauthorizedException) = mapOf(
        "code" to "UNAUTHORIZED",
        "message" to e.message
    )

    @ExceptionHandler(RateLimitException::class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    open fun onRateLimitExceeded(e: RateLimitException) = mapOf(
        "code" to "RATE_LIMIT_EXCEEDED",
        "message" to e.message
    )

    @ExceptionHandler(EmailNotVerifiedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    open fun onEmailNotVerified(e: EmailNotVerifiedException) = mapOf(
        "code" to "EMAIL_NOT_VERIFIED",
        "message" to e.message
    )

    @ExceptionHandler(SamePasswordException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    open fun onSamePassword(e: SamePasswordException) = mapOf(
        "code" to "SAME_PASSWORD",
        "message" to e.message
    )
    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    open fun onUserNotFound(e: UserNotFoundException) = mapOf(
        "code" to "USER_NOT_FOUND",
        "message" to e.message
    )
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(e: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }


        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(mapOf(
                "errors" to errors,
                "code" to "VALIDATION_ERROR"
            ))
    }
}