package com.gideon.chirp.domain.events

import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val messageId: ChatMessageId,
    val chatId: ChatId,
)
