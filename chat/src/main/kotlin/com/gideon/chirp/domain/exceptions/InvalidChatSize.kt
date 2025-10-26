package com.gideon.chirp.domain.exceptions

class InvalidChatSizeException: RuntimeException("There must be at least two unique participant to create a chat") {
}