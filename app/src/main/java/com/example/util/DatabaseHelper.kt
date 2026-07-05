package com.example.util

import android.content.Context
import android.net.Uri
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object DatabaseHelper {
    private const val DB_NAME = "alqadi_database.db"

    fun backupDatabase(context: Context, destinationUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) return false

            val inputStream = FileInputStream(dbFile)
            val outputStream = context.contentResolver.openOutputStream(destinationUri)

            if (outputStream != null) {
                copyStream(inputStream, outputStream)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun restoreDatabase(context: Context, sourceUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            
            // Close any open connections by completely deleting the database 
            // (this works best if the app restarts after, or if we ensure Room is closed)
            context.deleteDatabase(DB_NAME)

            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(dbFile)

            if (inputStream != null) {
                copyStream(inputStream, outputStream)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearDatabase(context: Context): Boolean {
        return try {
            context.deleteDatabase(DB_NAME)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
        output.flush()
        output.close()
        input.close()
    }
}
