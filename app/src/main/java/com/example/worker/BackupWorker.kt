package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = context.applicationContext as com.example.AlQadiApplication
            app.container.database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

            val dbFile = context.getDatabasePath("alqadi_database.db")
            if (dbFile.exists()) {
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                
                val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val backupFile = File(backupDir, "backup_$dateStr.db")
                
                FileInputStream(dbFile).use { inputStream ->
                    FileOutputStream(backupFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                // Simulate cloud upload to Google Drive
                delay(2000)
                // Note: For actual Google Drive background upload without user prompt, 
                // Google Drive REST API & OAuth2 server-side tokens are required.
                // This local backup acts as the automated job.
                
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
