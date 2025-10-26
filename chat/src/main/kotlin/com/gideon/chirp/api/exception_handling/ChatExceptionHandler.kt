package com.gideon.chirp.api.exception_handling

import com.gideon.chirp.domain.exception.ForbiddenException
import com.gideon.chirp.domain.exceptions.ChatMessageNotFoundException
import com.gideon.chirp.domain.exceptions.ChatNotFoundException
import com.gideon.chirp.domain.exceptions.ChatParticipantNotFoundException
import com.gideon.chirp.domain.exceptions.InvalidChatSizeException
import com.gideon.chirp.domain.exceptions.InvalidProfilePictureException
import com.gideon.chirp.domain.exceptions.StorageException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        ChatMessageNotFoundException::class,
        ChatParticipantNotFoundException::class
        )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(e: Exception) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )
    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidRequest(e: ForbiddenException) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )

    @ExceptionHandler(InvalidProfilePictureException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidProfilePicture(e: InvalidProfilePictureException) = mapOf(
        "code" to "INVALID_PROFILE_PICTURE",
        "message" to e.message
    )

    @ExceptionHandler(StorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun onStorage(e: StorageException) = mapOf(
        "code" to "STORAGE_ERROR",
        "message" to e.message
    )
}