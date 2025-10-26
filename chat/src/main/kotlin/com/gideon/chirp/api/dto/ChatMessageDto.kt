package com.gideon.chirp.api.dto

import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.ChatMessageId
import com.gideon.chirp.domain.type.UserId
import java.time.Instant

data class ChatMessageDto(
    val id: ChatMessageId,
    val chatId: ChatId,
    val content: String,
    val createdAt: Instant,
    val senderId: UserId,
)
