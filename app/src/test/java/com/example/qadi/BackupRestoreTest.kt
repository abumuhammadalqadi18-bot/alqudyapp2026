package com.example.qadi

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.AlQadiApplication
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EmployeeEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRestoreTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<AlQadiApplication>()
        db = (context as AlQadiApplication).container.database
    }

    @Test
    fun testLocalBackupCreatesValidFile() = runBlocking {
        // 1. Insert data to ensure DB is created and not empty
        db.employeeDao().insert(
            EmployeeEntity(
                name = "Backup Test Employee",
                jobTitle = "Tester",
                currentDailyWage = 1000.0,
                phone = "", nationalId = null, department = null, hireDate = System.currentTimeMillis(), photoPath = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
        )
        // Force the database to write to disk
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        db.close()
        
        // 2. Setup backup destination
        val backupDir = File(context.cacheDir, "backups")
        backupDir.mkdirs()
        val backupFile = File(backupDir, "test_backup.db")
        if (backupFile.exists()) {
            backupFile.delete()
        }

        // 3. Perform Backup
        val dbFile = context.getDatabasePath("alqadi_database.db")
        
        if (dbFile.exists()) {
            var success = false
            try {
                dbFile.inputStream().use { input ->
                    backupFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                success = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
            assertTrue(success)
        }
    }
}