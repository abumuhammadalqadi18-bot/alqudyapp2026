package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.BackupLocation
import com.example.domain.model.BackupStatus

@Entity(tableName = "backup_history")
data class BackupHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "backup_date")
    val backupDate: Long,
    @ColumnInfo(name = "backup_location")
    val backupLocation: BackupLocation,
    @ColumnInfo(name = "backup_size")
    val backupSize: Long,
    val status: BackupStatus
)
