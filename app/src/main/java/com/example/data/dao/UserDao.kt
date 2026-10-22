package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserAccount?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: String, status: UserStatus)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: String, role: UserRole)

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE id = :userId")
    suspend fun updateUserPassword(userId: String, newPasswordHash: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)
}
