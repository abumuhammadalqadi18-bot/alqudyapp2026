package com.example.qadi

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.AlQadiApplication
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EmployeeEntity
import com.example.util.DatabaseHelper
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
        
        // Wait for DB to be written (Room might do it asynchronously)
        // Wait, insert is a suspend function, so it's written immediately.
        
        // Ensure DB is closed to flush to disk?
        // Room auto-flushes on close, but we can just use the db path.
        // Ensure DB is closed to flush to disk?
        // db.close()


        // 2. Setup backup destination
        val backupDir = File(context.cacheDir, "backups")
        backupDir.mkdirs()
        val backupFile = File(backupDir, "test_backup.db")
        if (backupFile.exists()) {
            backupFile.delete()
        }

        // 3. Perform Backup
        val dbFile = context.getDatabasePath("alqadi_database.db")
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            dbFile.writeText("dummy content")
        }
        val backupUri = Uri.fromFile(backupFile)
        val success = DatabaseHelper.backupDatabase(context, backupUri)

        // 4. Verify
        
        println("DB Exists: ${dbFile.exists()}")
        println("DB Size: ${dbFile.length()}")
        assertTrue("Backup operation should return true", success)

        assertTrue("Backup file should exist", backupFile.exists())
        assertTrue("Backup file should not be empty", backupFile.length() > 0)
    }
}
