package com.example.data.repository

import com.example.data.local.dao.AdjustmentDao
import com.example.data.local.dao.WithdrawalDao
import com.example.data.local.entity.AdjustmentEntity
import com.example.data.local.entity.WithdrawalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for managing financial transactions (Withdrawals and Adjustments).
 * مستودع إدارة الحركات المالية (السحوبات المالية والتعديلات كالمكافآت والخصومات).
 */
class FinanceRepository(
    private val withdrawalDao: WithdrawalDao,
    private val adjustmentDao: AdjustmentDao
) {

    // --- Withdrawals (السحوبات) ---
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long = withdrawalDao.insert(withdrawal)

    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity) = withdrawalDao.update(withdrawal)

    suspend fun deleteWithdrawal(withdrawal: WithdrawalEntity) = withdrawalDao.delete(withdrawal)

    fun getWithdrawalsForEmployee(employeeId: Long): Flow<List<WithdrawalEntity>> = 
        withdrawalDao.getWithdrawalsForEmployee(employeeId)

    fun getWithdrawalsInRange(startDate: Long, endDate: Long): Flow<List<WithdrawalEntity>> = 
        withdrawalDao.getWithdrawalsInRange(startDate, endDate)

    fun getWithdrawalsForEmployeeInRange(employeeId: Long, startDate: Long, endDate: Long): Flow<List<WithdrawalEntity>> =
        withdrawalDao.getWithdrawalsForEmployeeInRange(employeeId, startDate, endDate)

    suspend fun getTotalWithdrawalForEmployee(employeeId: Long): Double = withdrawalDao.getTotalWithdrawalForEmployee(employeeId)

    suspend fun getTotalWithdrawalsInRange(startDate: Long, endDate: Long): Double = withdrawalDao.getTotalWithdrawalsInRange(startDate, endDate)

    suspend fun getTotalWithdrawalsAll(): Double = withdrawalDao.getTotalWithdrawalsAll()

    // --- Adjustments (التعديلات - مكافأة/خصم) ---
    suspend fun insertAdjustment(adjustment: AdjustmentEntity): Long = adjustmentDao.insert(adjustment)

    suspend fun updateAdjustment(adjustment: AdjustmentEntity) = adjustmentDao.update(adjustment)

    suspend fun deleteAdjustment(adjustment: AdjustmentEntity) = adjustmentDao.delete(adjustment)

    fun getAdjustmentsForEmployee(employeeId: Long): Flow<List<AdjustmentEntity>> = 
        adjustmentDao.getAdjustmentsForEmployee(employeeId)

    fun getAdjustmentsInRange(startDate: Long, endDate: Long): Flow<List<AdjustmentEntity>> = 
        adjustmentDao.getAdjustmentsInRange(startDate, endDate)

    fun getAdjustmentsForEmployeeInRange(employeeId: Long, startDate: Long, endDate: Long): Flow<List<AdjustmentEntity>> =
        adjustmentDao.getAdjustmentsForEmployeeInRange(employeeId, startDate, endDate)

    suspend fun getTotalBonusesForEmployee(employeeId: Long): Double = adjustmentDao.getTotalBonusesForEmployee(employeeId)

    suspend fun getTotalDeductionsForEmployee(employeeId: Long): Double = adjustmentDao.getTotalDeductionsForEmployee(employeeId)

    suspend fun getTotalBonusesInRange(startDate: Long, endDate: Long): Double = adjustmentDao.getTotalBonusesInRange(startDate, endDate)

    suspend fun getTotalDeductionsInRange(startDate: Long, endDate: Long): Double = adjustmentDao.getTotalDeductionsInRange(startDate, endDate)

    suspend fun getTotalBonusesAll(): Double = adjustmentDao.getTotalBonusesAll()

    suspend fun getTotalDeductionsAll(): Double = adjustmentDao.getTotalDeductionsAll()
}
