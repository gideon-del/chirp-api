package com.gideon.chirp.infra.database.mappers

import com.gideon.chirp.domain.models.Chat
import com.gideon.chirp.domain.models.ChatMessage
import com.gideon.chirp.domain.models.ChatParticipant
import com.gideon.chirp.infra.database.entities.ChatEntity
import com.gideon.chirp.infra.database.entities.ChatMessageEntity
import com.gideon.chirp.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        participants = participants.map {
            it.toChartParticipant()
        }.toSet(),
        creator = creator.toChartParticipant(),
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        lastMessage = lastMessage,
        createdAt = createdAt

    )
}

fun ChatParticipantEntity.toChartParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}


fun ChatParticipant.toChartParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl,

    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id!!,
        sender = sender.toChartParticipant(),
        chatId = chatId,
        content = content,
        createdAt = createdAt
    )
}