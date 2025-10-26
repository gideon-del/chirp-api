package com.gideon.chirp.infra.database.mappers

import com.gideon.chirp.domain.models.EmailVerificationToken
import com.gideon.chirp.infra.database.entities.EmailVerificationTokenEntity


fun EmailVerificationTokenEntity.toEmailVerification(): EmailVerificationToken {
    return EmailVerificationToken(
        id=id,
        user=user.toUser(),
        token = token
    )
}