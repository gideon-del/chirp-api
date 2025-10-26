package com.gideon.chirp.domain.exception

import java.lang.RuntimeException

class InvalidTokenException(override  val message: String?): RuntimeException(
    message ?: "Invalid token"
)