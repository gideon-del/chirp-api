package com.gideon.chirp.infra.database.repositories

import com.gideon.chirp.domain.type.UserId
import com.gideon.chirp.infra.database.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.sql.Ref

interface RefreshTokenRepository: JpaRepository<RefreshTokenEntity, Long> {
    fun findByUserIdAndHashedToken(userId: UserId, hashedToken: String): RefreshTokenEntity?
    fun deleteByUserIdAndHashedToken(userId: UserId, hashedToken: String)
    fun  deleteByUserId(userId: UserId)
}