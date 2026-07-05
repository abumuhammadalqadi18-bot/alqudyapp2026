package com.example.data.repository

import com.example.data.local.dao.WageRateHistoryDao
import com.example.data.local.dao.WageRecordDao
import com.example.data.local.entity.WageRateHistoryEntity
import com.example.data.local.entity.WageRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for managing daily wage records and wage rate history.
 * مستودع إدارة سجلات الأجور اليومية وتاريخ تغيير أجور الموظفين.
 */
class WageRepository(
    private val wageRecordDao: WageRecordDao,
    private val wageRateHistoryDao: WageRateHistoryDao
) {

    // --- Wage Records ---
    suspend fun insertWageRecord(record: WageRecordEntity): Long = wageRecordDao.insert(record)

    suspend fun insertWageRecords(records: List<WageRecordEntity>) = wageRecordDao.insertAll(records)

    suspend fun updateWageRecord(record: WageRecordEntity) = wageRecordDao.update(record)

    suspend fun deleteWageRecord(record: WageRecordEntity) = wageRecordDao.delete(record)

    fun getRecordsForEmployee(employeeId: Long): Flow<List<WageRecordEntity>> = 
        wageRecordDao.getRecordsForEmployee(employeeId)

    fun getRecordsInRange(startDate: Long, endDate: Long): Flow<List<WageRecordEntity>> = 
        wageRecordDao.getRecordsInRange(startDate, endDate)

    fun getRecordsForEmployeeInRange(employeeId: Long, startDate: Long, endDate: Long): Flow<List<WageRecordEntity>> = 
        wageRecordDao.getRecordsForEmployeeInRange(employeeId, startDate, endDate)

    // --- Wage Rate History ---
    suspend fun insertWageRateHistory(history: WageRateHistoryEntity): Long = wageRateHistoryDao.insert(history)

    fun getWageRateHistoryForEmployee(employeeId: Long): Flow<List<WageRateHistoryEntity>> = 
        wageRateHistoryDao.getHistoryForEmployee(employeeId)
}
