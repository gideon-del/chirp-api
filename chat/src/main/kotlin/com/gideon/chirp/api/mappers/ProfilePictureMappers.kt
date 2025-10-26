package com.gideon.chirp.api.mappers

import com.gideon.chirp.api.dto.PictureUploadResponse
import com.gideon.chirp.domain.models.ProfilePictureUploadCredentials

fun ProfilePictureUploadCredentials.toResponse(): PictureUploadResponse {
    return PictureUploadResponse(
        uploadUrl = uploadUrl,
        expiresAt =expiresAt,
        publicUrl =  publicUrl,
        headers = headers
    )
}