package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WageRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WageRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<WageRecordEntity>)

    @Update
    suspend fun update(record: WageRecordEntity)

    @Delete
    suspend fun delete(record: WageRecordEntity)

    @Query("SELECT * FROM wage_records WHERE employee_id = :employeeId ORDER BY wage_date DESC")
    fun getRecordsForEmployee(employeeId: Long): Flow<List<WageRecordEntity>>

    @Query("SELECT * FROM wage_records WHERE wage_date >= :startDate AND wage_date <= :endDate ORDER BY wage_date DESC")
    fun getRecordsInRange(startDate: Long, endDate: Long): Flow<List<WageRecordEntity>>

    @Query("SELECT * FROM wage_records WHERE employee_id = :employeeId AND wage_date >= :startDate AND wage_date <= :endDate ORDER BY wage_date DESC")
    fun getRecordsForEmployeeInRange(employeeId: Long, startDate: Long, endDate: Long): Flow<List<WageRecordEntity>>
}
