package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BackupHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(backupHistory: BackupHistoryEntity): Long

    @Query("SELECT * FROM backup_history ORDER BY backup_date DESC")
    fun getAllBackupHistory(): Flow<List<BackupHistoryEntity>>
    
    @Query("SELECT * FROM backup_history ORDER BY backup_date DESC LIMIT 1")
    suspend fun getLastBackup(): BackupHistoryEntity?
}
