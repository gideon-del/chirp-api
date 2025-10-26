package com.gideon.chirp.domain.exception

import java.lang.RuntimeException


class EmailNotVerifiedException: RuntimeException(
    "Email is not verified"
)