package com.example.data.repository

import com.example.data.local.dao.BackupHistoryDao
import com.example.data.local.entity.BackupHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for managing backup history records.
 * مستودع إدارة سجلات النسخ الاحتياطي (محلي أو عبر جوجل درايف).
 */
class BackupRepository(private val backupHistoryDao: BackupHistoryDao) {

    suspend fun recordBackup(backupHistory: BackupHistoryEntity): Long {
        return backupHistoryDao.insert(backupHistory)
    }

    fun getAllBackupHistory(): Flow<List<BackupHistoryEntity>> = backupHistoryDao.getAllBackupHistory()

    suspend fun getLastBackup(): BackupHistoryEntity? = backupHistoryDao.getLastBackup()
}
