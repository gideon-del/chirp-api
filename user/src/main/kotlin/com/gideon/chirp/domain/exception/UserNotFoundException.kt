package com.gideon.chirp.domain.exception

class UserNotFoundException: RuntimeException(
    "User not found"
)