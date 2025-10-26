package com.gideon.chirp.api.dto.ws

import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.ChatMessageId

data class SendMessageDto(
    val chatId: ChatId,
    val messageId: ChatMessageId? = null,
   val content: String
)