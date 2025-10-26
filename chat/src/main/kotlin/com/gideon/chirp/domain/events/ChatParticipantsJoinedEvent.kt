package com.gideon.chirp.domain.events

import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)