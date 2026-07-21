package com.example.util

import android.content.Context
import android.net.Uri
import com.example.ui.viewmodels.EmployeeDetail
import com.example.ui.viewmodels.GeneralEmployeeRecord
import com.example.ui.viewmodels.TransactionItem
import com.example.ui.viewmodels.TransactionType
import java.io.OutputStreamWriter

object CsvExportHelper {

    fun exportEmployeeStatementCsv(
        context: Context,
        uri: Uri,
        employee: EmployeeDetail,
        transactions: List<TransactionItem>,
        currency: String
    ) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                val writer = OutputStreamWriter(out, "UTF-8")
                // Write BOM for Excel to recognize UTF-8 correctly
                writer.write('\uFEFF'.toString())
                
                writer.write("كشف حساب عامل\n")
                writer.write("اسم العامل:,${employee.name}\n")
                writer.write("المهنة:,${employee.jobTitle.ifEmpty { "غير محدد" }}\n")
                writer.write("معدل الأجرة:,${employee.dailyWage} $currency\n\n")

                // Table Header
                writer.write("التاريخ,البيان,نوع الحركة,المبلغ ($currency)\n")

                // Transactions
                for (tx in transactions) {
                    val dateStr = tx.date.toUtcDateString()
                    val desc = tx.description.replace(",", " ") // prevent CSV breaking
                    val typeStr = when (tx.type) {
                        TransactionType.WAGE -> "أجرة"
                        TransactionType.BONUS -> "مكافأة"
                        TransactionType.WITHDRAWAL -> "سحب/سلفة"
                        TransactionType.DEDUCTION -> "خصم"
                    }
                    val isIncome = tx.type == TransactionType.WAGE || tx.type == TransactionType.BONUS
                    val prefix = if (isIncome) "+" else "-"
                    
                    writer.write("$dateStr,$desc,$typeStr,$prefix${tx.amount}\n")
                }
                
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportGeneralReportCsv(
        context: Context,
        uri: Uri,
        records: List<GeneralEmployeeRecord>,
        filterName: String,
        currency: String
    ) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                val writer = OutputStreamWriter(out, "UTF-8")
                // Write BOM
                writer.write('\uFEFF'.toString())
                
                writer.write("التقرير الدوري العام للتكاليف\n")
                writer.write("الفترة:,$filterName\n\n")

                // Table Header
                writer.write("اسم العامل,أيام الحضور,الإجمالي المستحق ($currency),تاريخ آخر تحديث\n")

                // Records
                for (record in records) {
                    val name = record.employeeName.replace(",", " ")
                    val dateStr = record.lastTransactionDate.toUtcDateString()
                    writer.write("$name,${record.attendanceDays},${record.totalAmount},$dateStr\n")
                }
                
                writer.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
