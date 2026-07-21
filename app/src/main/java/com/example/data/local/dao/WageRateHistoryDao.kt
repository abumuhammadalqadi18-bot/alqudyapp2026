package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WageRateHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WageRateHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WageRateHistoryEntity): Long

    @Query("SELECT * FROM wage_rate_history WHERE employee_id = :employeeId ORDER BY effective_from_date DESC")
    fun getHistoryForEmployee(employeeId: Long): Flow<List<WageRateHistoryEntity>>
}
