package com.gideon.chirp.service

import com.gideon.chirp.api.dto.ChatMessageDto
import com.gideon.chirp.api.mappers.toChatMessageDto
import com.gideon.chirp.domain.events.ChatCreatedEvent
import com.gideon.chirp.domain.events.ChatParticipantLeftEvent
import com.gideon.chirp.domain.events.ChatParticipantsJoinedEvent
import com.gideon.chirp.domain.exception.ForbiddenException
import com.gideon.chirp.domain.exceptions.ChatNotFoundException
import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.domain.exceptions.ChatParticipantNotFoundException
import com.gideon.chirp.domain.exceptions.InvalidChatSizeException
import com.gideon.chirp.domain.models.Chat
import com.gideon.chirp.domain.models.ChatMessage
import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.infra.database.entities.ChatEntity
import com.gideon.chirp.infra.database.mappers.toChat
import com.gideon.chirp.infra.database.mappers.toChatMessage
import com.gideon.chirp.infra.database.repository.ChatMessageRepository
import com.gideon.chirp.infra.database.repository.ChatParticipantRepository
import com.gideon.chirp.infra.database.repository.ChatRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
  private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Cacheable(
        value = ["messages"],
        key = "#chatId",
        condition = "#before == null && #pageSize <= 50",
        sync = true
    )
    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository.findByChatIdBefore(
            chatId = chatId,
            before = before ?: Instant.now(),
            pageable = PageRequest.of(0, pageSize),

            ).content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto()}
    }
@Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,

    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

       val allParticipants = (otherParticipants + creatorId)
    if(allParticipants.size < 2){
        throw InvalidChatSizeException()
    }

    val creator = chatParticipantRepository.findByIdOrNull(creatorId)
        ?: throw ChatParticipantNotFoundException(creatorId)

    return chatRepository.saveAndFlush(
        ChatEntity(
            creator= creator,
            participants = setOf(creator) + otherParticipants,

        )
    ).toChat(null).also { chat ->
        applicationEventPublisher.publishEvent(ChatCreatedEvent(
            chatId = chat.id,
            participantIds = chat.participants.map { it.userId }
        ))
    }
    }
    @Transactional
    fun addParticipantToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()
        val isRequestingUserInChat = chat.participants.any{
            it.userId == requestUserId
        }

        if(!isRequestingUserInChat){
            throw ForbiddenException()
        }
        val lastMessage = lastMessageForChat(chatId)
        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage)
applicationEventPublisher.publishEvent(
    ChatParticipantsJoinedEvent(
        chatId = chatId,
         userIds = userIds
    )
)
        return updatedChat
    }
    fun getChatById(
        chatId: ChatId,
        requestUserId: UserId
        ): Chat? {
        return chatRepository.findChatById(chatId,requestUserId)?.toChat(lastMessageForChat(chatId))
    }
    fun findChatsByUser(
        userId: UserId
    ): List<Chat> {
        val chatEntities = chatRepository.findAllByUserId(userId)

        val chatIds = chatEntities.mapNotNull { it.id }

        val lastMessages = chatMessageRepository
            .findLatestMessagesByChatIds(chatIds.toSet())
            .associateBy { it.chatId }

        return chatEntities
            .map {
                it.toChat(
                    lastMessage  = lastMessages[it.id]?.toChatMessage()
                )
            }
            .sortedByDescending { it.lastActivityAt }
    }
    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val participant = chat.participants
            .find {
                it.userId == userId
            } ?: throw ChatParticipantNotFoundException(userId)
        val newParticipantSize = chat.participants.size - 1
        if(newParticipantSize == 0){
            chatRepository.deleteById(chatId)
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participants
            }
        )
        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId= chatId,
                userId = userId
            )
        )


    }
    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }


}