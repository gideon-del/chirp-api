package com.gideon.chirp.service

import com.gideon.chirp.api.dto.ChatMessageDto
import com.gideon.chirp.api.mappers.toChatMessageDto
import com.gideon.chirp.domain.events.MessageDeletedEvent
import com.gideon.chirp.domain.events.chat.ChatEvent
import com.gideon.chirp.domain.exception.ForbiddenException
import com.gideon.chirp.domain.exceptions.ChatMessageNotFoundException
import com.gideon.chirp.domain.exceptions.ChatNotFoundException
import com.gideon.chirp.domain.exceptions.ChatParticipantNotFoundException
import com.gideon.chirp.domain.models.ChatMessage
import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.domain.type.ChatMessageId
import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.infra.database.entities.ChatMessageEntity
import com.gideon.chirp.infra.database.mappers.toChatMessage
import com.gideon.chirp.infra.database.repository.ChatMessageRepository
import com.gideon.chirp.infra.database.repository.ChatParticipantRepository
import com.gideon.chirp.infra.database.repository.ChatRepository
import com.gideon.chirp.infra.message_queue.EventPublisher
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublisher: EventPublisher
) {


    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                sender = sender
            )
        )
        eventPublisher.publish(
            ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername =  sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = content
            )
        )
      return  savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
     val message = chatMessageRepository.findByIdOrNull(messageId)
         ?: throw ChatMessageNotFoundException(messageId)

        if(message.sender.userId != requestUserId){
            throw ForbiddenException()
        }
        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                chatId = message.chatId,
                messageId = message.id!!
            )
        )
        evictMessagesCache(message.chatId)
    }
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: let's spring handle the cache evict
    }
}