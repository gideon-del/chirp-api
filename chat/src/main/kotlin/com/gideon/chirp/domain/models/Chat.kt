package com.gideon.chirp.domain.models

import com.gideon.chirp.domain.type.ChatId
import java.time.Instant

data class Chat(
    val id: ChatId,
    val participants: Set<ChatParticipant>,
    val lastMessage: ChatMessage? = null,
    val creator: ChatParticipant,
    val lastActivityAt: Instant,
    val createdAt: Instant
)
