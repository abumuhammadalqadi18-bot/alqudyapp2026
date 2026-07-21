package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdjustmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(adjustment: AdjustmentEntity): Long

    @Update
    suspend fun update(adjustment: AdjustmentEntity)

    @Delete
    suspend fun delete(adjustment: AdjustmentEntity)

    @Query("SELECT * FROM adjustments WHERE employee_id = :employeeId ORDER BY adjustment_date DESC")
    fun getAdjustmentsForEmployee(employeeId: Long): Flow<List<AdjustmentEntity>>

    @Query("SELECT * FROM adjustments WHERE adjustment_date >= :startDate AND adjustment_date <= :endDate ORDER BY adjustment_date DESC")
    fun getAdjustmentsInRange(startDate: Long, endDate: Long): Flow<List<AdjustmentEntity>>

    @Query("SELECT * FROM adjustments WHERE employee_id = :employeeId AND adjustment_date >= :startDate AND adjustment_date <= :endDate ORDER BY adjustment_date DESC")
    fun getAdjustmentsForEmployeeInRange(employeeId: Long, startDate: Long, endDate: Long): Flow<List<AdjustmentEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM adjustments WHERE employee_id = :employeeId AND type = 'BONUS'")
    suspend fun getTotalBonusesForEmployee(employeeId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM adjustments WHERE employee_id = :employeeId AND type = 'DEDUCTION'")
    suspend fun getTotalDeductionsForEmployee(employeeId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM adjustments WHERE adjustment_date >= :startDate AND adjustment_date <= :endDate AND type = 'BONUS'")
    suspend fun getTotalBonusesInRange(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM adjustments WHERE adjustment_date >= :startDate AND adjustment_date <= :endDate AND type = 'DEDUCTION'")
    suspend fun getTotalDeductionsInRange(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM adjustments WHERE type = 'BONUS'")
    suspend fun getTotalBonusesAll(): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM adjustments WHERE type = 'DEDUCTION'")
    suspend fun getTotalDeductionsAll(): Double
}
