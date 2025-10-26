package com.gideon.chirp.api.dto.ws

import com.gideon.chirp.domain.type.ChatId

data class ChatParticipantChangedDto(
    val chatId: ChatId
)
