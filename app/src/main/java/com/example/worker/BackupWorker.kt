package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.local.entity.BackupHistoryEntity
import com.example.domain.model.BackupLocation
import com.example.domain.model.BackupStatus

class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "BackupWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val app = context.applicationContext as com.example.AlQadiApplication

            try {
                app.container.database.openHelper.writableDatabase
                    .query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
            } catch (e: Exception) {
                Log.e(TAG, "WAL checkpoint failed", e)
                return Result.failure()
            }

            val dbFile = context.getDatabasePath("alqadi_database.db")
            if (!dbFile.exists()) {
                Log.w(TAG, "Database file not found: ${dbFile.absolutePath}")
                return Result.failure()
            }

            val backupDir = File(context.getExternalFilesDir(null), "backups")
            try {
                if (!backupDir.exists() && !backupDir.mkdirs()) {
                    Log.e(TAG, "Failed to create backup directory: ${backupDir.absolutePath}")
                    return Result.failure()
                }
            } catch (e: IOException) {
                Log.e(TAG, "IOException creating backup directory", e)
                return Result.failure()
            }

            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupBaseName = "backup_$dateStr"
            val backupFile = File(backupDir, "$backupBaseName.db")

            try {
                FileInputStream(dbFile).use { inputStream ->
                    FileOutputStream(backupFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to copy database file", e)
                return Result.failure()
            }

            val walFile = File(dbFile.path + "-wal")
            if (walFile.exists()) {
                try {
                    val backupWalFile = File(backupDir, "$backupBaseName.db-wal")
                    FileInputStream(walFile).use { inputStream ->
                        FileOutputStream(backupWalFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to copy WAL file", e)
                    return Result.failure()
                }
            }

            val shmFile = File(dbFile.path + "-shm")
            if (shmFile.exists()) {
                try {
                    val backupShmFile = File(backupDir, "$backupBaseName.db-shm")
                    FileInputStream(shmFile).use { inputStream ->
                        FileOutputStream(backupShmFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to copy SHM file", e)
                    return Result.failure()
                }
            }

            try {
                app.container.backupRepository.recordBackup(
                    BackupHistoryEntity(
                        backupDate = System.currentTimeMillis(),
                        backupLocation = BackupLocation.LOCAL,
                        backupSize = backupFile.length(),
                        status = BackupStatus.SUCCESS
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record backup history", e)
                return Result.failure()
            }

            Log.i(TAG, "Backup completed successfully: ${backupFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during backup", e)
            Result.failure()
        }
    }
}
