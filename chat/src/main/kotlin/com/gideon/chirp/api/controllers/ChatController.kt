package com.gideon.chirp.api.controllers

import com.gideon.chirp.api.dto.AddParticipantToChatDto
import com.gideon.chirp.api.util.requestUserId
import com.gideon.chirp.api.dto.ChatDto
import com.gideon.chirp.api.dto.ChatMessageDto
import com.gideon.chirp.api.dto.CreateChatRequest
import com.gideon.chirp.api.mappers.toChatDto
import com.gideon.chirp.domain.type.ChatId
import com.gideon.chirp.service.ChatMessageService
import com.gideon.chirp.service.ChatService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {
    companion object{
        private const val DEFAULT_PAGE_SIZE = 20

    }

    @GetMapping("/{chatId}/messages")
    fun getMessagesForChat(
        @PathVariable("chatId") chatId: ChatId,
        @RequestParam("before", required = false) before: Instant? = null,
        @RequestParam("pageSize", required = false) pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<ChatMessageDto>{
        return chatService.getChatMessages(
            chatId =chatId,
            before =before,
            pageSize = pageSize
        )
    }

    @GetMapping("/{chatId}")
    fun getChat(
        @PathVariable("chatId") chatId: ChatId
    ): ChatDto {
        return chatService.getChatById(
            chatId,
            requestUserId
        )?.toChatDto() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @GetMapping
    fun getChatsForUser(): List<ChatDto> {
        return chatService.findChatsByUser(
           userId= requestUserId
        ).map { it.toChatDto() }
    }
 @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto{
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
    @PostMapping("/{chatId}/add")
    fun addChatParticipant(
        @PathVariable chatId: ChatId,
        @Valid @RequestBody body: AddParticipantToChatDto
    ): ChatDto {
        return  chatService.addParticipantToChat(
            requestUserId= requestUserId,
            chatId = chatId,
            userIds = body.userIds.toSet()
        ).toChatDto()
    }

    @DeleteMapping("/{chatId}/leave")
    fun removeChatParticipant(
        @PathVariable chatId: ChatId,

    ) {
          chatService.removeParticipantFromChat(
            userId =  requestUserId,
            chatId = chatId,

        )
    }
}