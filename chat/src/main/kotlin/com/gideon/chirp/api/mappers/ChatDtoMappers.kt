package com.gideon.chirp.api.mappers

import com.gideon.chirp.api.dto.ChatDto
import com.gideon.chirp.api.dto.ChatMessageDto
import com.gideon.chirp.api.dto.ChatParticipantDto
import com.gideon.chirp.domain.models.Chat
import com.gideon.chirp.domain.models.ChatMessage
import com.gideon.chirp.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = participants.map {
            it.toChartParticipantDto()
        },
        lastActivityAt = lastActivityAt,
        lastMessage =lastMessage?.toChatMessageDto(),
        creator = creator.toChartParticipantDto()
    )
}
fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId,
    )
}
fun ChatParticipant.toChartParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}