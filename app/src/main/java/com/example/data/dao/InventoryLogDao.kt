package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.InventoryLog
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryLogDao {
    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun observeAllLogs(): Flow<List<InventoryLog>>

    @Query("SELECT * FROM inventory_logs WHERE productId = :productId ORDER BY timestamp DESC")
    fun observeLogsForProduct(productId: String): Flow<List<InventoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InventoryLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<InventoryLog>)

    @Query("DELETE FROM inventory_logs")
    suspend fun clearAll()
}
