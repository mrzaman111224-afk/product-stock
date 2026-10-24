package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun observeTransactionsSince(sinceTimestamp: Long): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord)

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE timestamp >= :sinceTimestamp")
    suspend fun getSalesTotalSince(sinceTimestamp: Long): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :sinceTimestamp")
    suspend fun getSalesCountSince(sinceTimestamp: Long): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
