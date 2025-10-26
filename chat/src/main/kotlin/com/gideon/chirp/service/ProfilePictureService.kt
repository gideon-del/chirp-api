package com.gideon.chirp.service

import com.gideon.chirp.domain.events.ProfilePictureUpdatedEvent
import com.gideon.chirp.domain.exceptions.ChatParticipantNotFoundException
import com.gideon.chirp.domain.exceptions.InvalidProfilePictureException
import com.gideon.chirp.domain.models.ProfilePictureUploadCredentials
import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.infra.database.repository.ChatParticipantRepository
import com.gideon.chirp.infra.storage.SuparbaseStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProfilePictureService(
    private val suparbaseStorageService: SuparbaseStorageService,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @param:Value("\${supabase.url}") private val supabseUrl: String,
) {

    private val logger = LoggerFactory.getLogger(ProfilePictureService::class.java)

    fun generateUploadCredentials(
        userId: UserId,
        mimeType: String
    ): ProfilePictureUploadCredentials {
        return suparbaseStorageService.generateSignedUploadUrl(
            userId = userId,
            mimeType = mimeType
        )
    }
    @Transactional
    fun deleteProfilePicture(userId: UserId) {

        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)

        participant.profilePictureUrl?.let { url ->
            chatParticipantRepository.save(
                participant.apply {
                    profilePictureUrl = null
                }
            )
            suparbaseStorageService.deleteFile(url)

            applicationEventPublisher.publishEvent(
                ProfilePictureUpdatedEvent(
                    userId = userId,
                    newUrl = null
                )
            )
        }



    }
    @Transactional
    fun confirmProfilePictureUpload(userId: UserId, publicUrl: String) {
        if(!publicUrl.startsWith(supabseUrl)){
            throw InvalidProfilePictureException("Invalid Profile picture url ")
        }
        val participant = chatParticipantRepository.findByIdOrNull(userId)
            ?: throw ChatParticipantNotFoundException(userId)
        val oldUrl = participant.profilePictureUrl
        chatParticipantRepository.save(
            participant.apply {
                profilePictureUrl = publicUrl
            }

        )
        try {
            oldUrl?.let {
                suparbaseStorageService.deleteFile(oldUrl)
            }
        }catch (e: Exception) {
            logger.warn("Deleting old profile picture for $userId failed", e)
        }

        applicationEventPublisher.publishEvent(
            ProfilePictureUpdatedEvent(
                userId = userId,
                newUrl = publicUrl
            )
        )
    }
}