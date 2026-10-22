package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    STAFF
}

enum class UserStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val status: UserStatus,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)
