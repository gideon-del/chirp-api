package com.gideon.chirp.domain.events.chat

import com.gideon.chirp.domain.events.ChirpEvent
import com.gideon.chirp.domain.events.user.UserEventConstants
import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed  class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = UserEventConstants.USER_EXCHANGE,
    override val occurredAt: Instant = Instant.now()
): ChirpEvent {
    data class NewMessage(
        val senderId: UserId,
        val senderUsername: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstant.CHAT_NEW_MESSAGE
    ): ChatEvent(), ChirpEvent

}