package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StoreSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreSettingDao {
    @Query("SELECT * FROM settings")
    fun observeAllSettings(): Flow<List<StoreSetting>>

    @Query("SELECT value FROM settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: StoreSetting)

    @Query("DELETE FROM settings WHERE `key` = :key")
    suspend fun deleteSetting(key: String)
}
