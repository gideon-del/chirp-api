package com.gideon.chirp.domain.exception

import java.lang.RuntimeException

class SamePasswordException: RuntimeException(
    "The new password can't be equal to the old one"
)