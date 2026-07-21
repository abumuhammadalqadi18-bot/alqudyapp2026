package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WithdrawalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun update(withdrawal: WithdrawalEntity)

    @Delete
    suspend fun delete(withdrawal: WithdrawalEntity)

    @Query("SELECT * FROM withdrawals WHERE employee_id = :employeeId ORDER BY withdrawal_date DESC")
    fun getWithdrawalsForEmployee(employeeId: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE withdrawal_date >= :startDate AND withdrawal_date <= :endDate ORDER BY withdrawal_date DESC")
    fun getWithdrawalsInRange(startDate: Long, endDate: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE employee_id = :employeeId AND withdrawal_date >= :startDate AND withdrawal_date <= :endDate ORDER BY withdrawal_date DESC")
    fun getWithdrawalsForEmployeeInRange(employeeId: Long, startDate: Long, endDate: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM withdrawals WHERE employee_id = :employeeId")
    suspend fun getTotalWithdrawalForEmployee(employeeId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM withdrawals WHERE withdrawal_date >= :startDate AND withdrawal_date <= :endDate")
    suspend fun getTotalWithdrawalsInRange(startDate: Long, endDate: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM withdrawals")
    suspend fun getTotalWithdrawalsAll(): Double
}
