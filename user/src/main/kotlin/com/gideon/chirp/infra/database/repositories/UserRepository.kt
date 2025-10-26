package com.gideon.chirp.infra.database.repositories


import com.gideon.chirp.infra.database.entities.UserEntity
import com.gideon.chirp.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository


interface UserRepository: JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?

}