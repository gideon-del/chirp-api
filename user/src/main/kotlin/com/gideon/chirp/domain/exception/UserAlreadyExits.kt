package com.gideon.chirp.domain.exception

import java.lang.RuntimeException

class UserAlreadyExitsException: RuntimeException (
    "A user with this username or email already exits"
)