package com.gideon.chirp.infra.storage

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.gideon.chirp.domain.exceptions.InvalidProfilePictureException
import com.gideon.chirp.domain.exceptions.StorageException
import com.gideon.chirp.domain.models.ProfilePictureUploadCredentials
import com.gideon.chirp.domain.type.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.UUID

@Service
class SuparbaseStorageService(
    @param:Value("\${supabase.url}") private val supabseUrl: String,
    private val supabaseRestClient: RestClient
) {


    companion object {
        private val allowedMimeTypes = mapOf(
            "image/jpeg" to "jpg",
            "image/jpg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp",
        )
    }

    fun generateSignedUploadUrl(userId: UserId,mimeType: String): ProfilePictureUploadCredentials {
        val extension = allowedMimeTypes[mimeType]
            ?: throw InvalidProfilePictureException("Invalid mime type $mimeType")

        val fileName = "user_${userId}_${UUID.randomUUID()}.$extension"
        val path = "profile-picture/$fileName"

        val publicUrl = "$supabseUrl/storage/v1/object/public/$path"
        return ProfilePictureUploadCredentials(
            uploadUrl = createSignedUrl(
                path = path,
                expiresInSecond = 300
            ),
            publicUrl = publicUrl,
            headers = mapOf(
                "Content-Type" to mimeType
            ),
            expiresAt = Instant.now().plusSeconds(300)
        )
    }
    fun deleteFile(url: String) {
        val path = if(url.contains("/object/public/")){
            url.substringAfter("/object/public/")
        } else throw StorageException("Invalid file Url Format")
        val deleteUrl = "/storage/v1/object/$path"

        val response = supabaseRestClient
            .delete()
            .uri(deleteUrl)
            .retrieve()
            .toBodilessEntity()

        if(response.statusCode.isError) {
            throw StorageException("Unable to delete file: ${response.statusCode.value()}")
        }
    }
    private fun createSignedUrl(
        path: String,
        expiresInSecond: Int
    ): String {
        val json = """
            { "expiresIn" : $expiresInSecond }
        """.trimIndent()
        val response = supabaseRestClient
            .post()
            .uri("/storage/v1/object/upload/sign/$path")
            .header("Content-Type","application/json")
            .body(json)
            .retrieve()
            .body(SignedUploadResponse::class.java)
            ?: throw StorageException("Failed to create signed url")

        return "$supabseUrl/storage/v1${response.url}"
    }
    private data class SignedUploadResponse @JsonCreator constructor(
        @JsonProperty("url")
        val url: String
    )
}