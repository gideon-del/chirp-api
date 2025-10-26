package com.gideon.chirp.domain.exceptions

import java.lang.RuntimeException

class InvalidProfilePictureException(override val message: String? = null): RuntimeException(message ?: "Invalid profile picture data") {
}