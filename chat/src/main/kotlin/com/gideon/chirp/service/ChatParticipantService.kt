package com.gideon.chirp.service

import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.domain.models.ChatParticipant
import com.gideon.chirp.infra.database.mappers.toChartParticipant
import com.gideon.chirp.infra.database.mappers.toChartParticipantEntity
import com.gideon.chirp.infra.database.repository.ChatParticipantRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {
    @Transactional
    fun createChatParticipant(
        chatParticipant: ChatParticipant
    ){
        chatParticipantRepository.save(
            chatParticipant.toChartParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChartParticipant()
    }
    fun findChatParticipantByEmailOrUsername(query: String): ChatParticipant? {
        val normalizeQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(normalizeQuery)?.toChartParticipant()
    }
}