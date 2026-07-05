package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.ui.viewmodels.EmployeeReportSummary
import java.io.File
import java.io.FileOutputStream

object CsvExporter {
    fun generateAndShareCsv(context: Context, summaries: List<EmployeeReportSummary>) {
        try {
            val file = File(context.cacheDir, "Financial_Report.csv")
            val outputStream = FileOutputStream(file)
            val writer = outputStream.writer(Charsets.UTF_8)
            
            // Write BOM for Excel UTF-8 recognition
            outputStream.write(0xEF)
            outputStream.write(0xBB)
            outputStream.write(0xBF)

            writer.append("الاسم,أيام الحضور,الأجور المستحقة,المسحوبات,الصافي المتبقي\n")

            for (summary in summaries) {
                writer.append("${summary.employeeName},${summary.attendanceDays},${summary.totalEarned},${summary.totalWithdrawn},${summary.netPayable}\n")
            }

            writer.flush()
            writer.close()
            outputStream.close()

            shareCsv(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareCsv(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "مشاركة التقرير (Excel/CSV)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
