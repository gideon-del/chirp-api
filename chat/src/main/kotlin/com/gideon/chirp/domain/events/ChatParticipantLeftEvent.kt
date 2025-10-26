package com.gideon.chirp.domain.events

import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
