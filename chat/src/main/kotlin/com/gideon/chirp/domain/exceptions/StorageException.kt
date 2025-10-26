package com.gideon.chirp.domain.exceptions


class StorageException(override val message: String?=null): RuntimeException(
    message ?: "Unable to store files"
) {
}