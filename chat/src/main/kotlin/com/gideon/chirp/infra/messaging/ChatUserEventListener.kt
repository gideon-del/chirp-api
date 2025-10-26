package com.gideon.chirp.infra.messaging

import com.gideon.chirp.domain.events.user.UserEvent
import com.gideon.chirp.infra.message_queue.MessageQueues
import com.gideon.chirp.domain.models.ChatParticipant
import com.gideon.chirp.service.ChatParticipantService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
) {
    private val logger = LoggerFactory.getLogger(ChatUserEventListener::class.java)
    @RabbitListener(queues = [MessageQueues.CHAT_USER_EVENTS])
    fun handleUserEvent(event: UserEvent){

        when(event){
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null
                    )
                )
            }
            else -> Unit
        }
    }
}