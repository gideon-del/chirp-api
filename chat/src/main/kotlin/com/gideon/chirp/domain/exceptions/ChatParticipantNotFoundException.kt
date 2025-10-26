package com.gideon.chirp.domain.exceptions

import com.gideon.chirp.domain.type.UserId

class ChatParticipantNotFoundException(private val id: UserId): RuntimeException("The chat participant with Id $id was not found") {
}