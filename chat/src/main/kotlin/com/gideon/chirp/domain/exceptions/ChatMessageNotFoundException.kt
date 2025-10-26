package com.gideon.chirp.domain.exceptions

import com.gideon.chirp.domain.type.ChatMessageId

class ChatMessageNotFoundException(
    private val id: ChatMessageId
): RuntimeException("Message with ID $id not found.") {
}