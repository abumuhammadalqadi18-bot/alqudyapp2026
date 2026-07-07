package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.viewmodels.EmployeeDetail
import com.example.ui.viewmodels.GeneralEmployeeRecord
import com.example.ui.viewmodels.ReportUiState
import com.example.ui.viewmodels.ReportViewModel
import com.example.ui.viewmodels.TransactionItem
import com.example.ui.viewmodels.TransactionType
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(reportViewModel: ReportViewModel) {
    val state by reportViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currency = LocalCurrencySymbol.current

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("كشف حساب عامل", "التقارير الدورية العامة")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "التقارير المالية",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = AccentGold
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) RoyalNavy else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = AccentGold,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (selectedTabIndex == 0) {
                    EmployeeStatementTab(state, reportViewModel, context, currency)
                } else {
                    GeneralReportsTab(state, reportViewModel, context, currency)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeStatementTab(
    state: ReportUiState,
    viewModel: ReportViewModel,
    context: Context,
    currency: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val selectedName = state.employees.find { it.id == state.selectedEmployeeId }?.name ?: ""
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("حدد العامل المطلوب *") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalNavy,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    state.employees.forEach { emp ->
                        DropdownMenuItem(
                            text = { Text("${emp.name} - ${emp.jobTitle.ifEmpty { "غير محدد" }}") },
                            onClick = {
                                viewModel.selectEmployee(emp.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (state.selectedEmployeeId != null && state.employeeSummary != null) {
            val empDetail = state.employees.find { it.id == state.selectedEmployeeId }
            if (empDetail != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoItem("المهنة", empDetail.jobTitle.ifEmpty { "غير محدد" })
                                InfoItem("معدل الأجرة", "${String.format("%,.2f", empDetail.dailyWage)} $currency")
                                InfoItem("تاريخ الالتحاق", empDetail.hireDate.toUtcDateString())
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            QuickActionItem("كشف منظم", "📄") { sharePdf(context, state.employeeTransactions, currency, empDetail.name) }
                            QuickActionItem("تصدير", "📊") { shareExcel(context, state.employeeTransactions, currency, empDetail.name) }
                            QuickActionItem("مشاركة", "🟢") {
                                val text = buildString {
                                    append("كشف حساب: ${empDetail.name}\n")
                                    append("الرصيد المتبقي: ${String.format("%,.2f", state.employeeSummary.netPayable)} $currency")
                                }
                                shareViaWhatsApp(context, text)
                            }
                            QuickActionItem("تنزيل", "📥") { sharePdf(context, state.employeeTransactions, currency, empDetail.name) }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "أجور مستحقة",
                                amount = "${String.format("%,.2f", state.employeeSummary.totalEarned)}",
                                color = RoyalNavy,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "سلفيات مقتطعة",
                                amount = "${String.format("%,.2f", state.employeeSummary.totalWithdrawn)}",
                                color = Color(0xFFC0473C),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "الرصيد المتبقي",
                                amount = "${String.format("%,.2f", state.employeeSummary.netPayable)}",
                                color = Color(0xFF2E7D5B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "سجل الحركات المالي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (state.employeeTransactions.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد حركات مسجلة لهذا العامل", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(state.employeeTransactions, key = { it.id }) { tx ->
                            TransactionRow(tx, currency)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("الرجاء تحديد عامل لعرض كشف الحساب", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralReportsTab(
    state: ReportUiState,
    viewModel: ReportViewModel,
    context: Context,
    currency: String
) {
    val filters = listOf("آخر 24 ساعة", "آخر 7 أيام", "آخر 30 يوم (تقرير شهري)")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = state.generalFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setGeneralFilter(filter) },
                        label = { Text(filter, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalNavy,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { /* Print Logic Placeholder */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("طباعة التقرير 🖨️", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        val text = "ملخص تقرير ${state.generalFilter}:\nإجمالي التكلفة التشغيلية: ${String.format("%,.2f", state.generalSummary.netCost)} $currency"
                        shareViaWhatsApp(context, text)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D5B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("مشاركة واتساب 🟢", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RoyalNavy.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, RoyalNavy.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ملخص الالتزامات الكلية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RoyalNavy)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("رواتب محتسبة ومستحقة الصرف:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%,.2f", state.generalSummary.totalEarned)} $currency", fontWeight = FontWeight.Bold, color = RoyalNavy)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي السلف والمقتطعات:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%,.2f", state.generalSummary.totalWithdrawn)} $currency", fontWeight = FontWeight.Bold, color = Color(0xFFC0473C))
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D5B).copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color(0xFF2E7D5B).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("السيولة وتوزيع المهام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D5B))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("صافي التكلفة التشغيلية:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%,.2f", state.generalSummary.netCost)} $currency", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D5B))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تعداد العمال المشاركين:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.generalSummary.participatingEmployeesCount} عامل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                "قائمة رواتب الأجور المسجلة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (state.generalRecords.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد سجلات أجور لهذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(state.generalRecords) { record ->
                GeneralRecordCard(record, currency)
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun QuickActionItem(label: String, iconStr: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(iconStr, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun StatCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(amount, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionItem, currency: String) {
    val isIncome = tx.type == TransactionType.WAGE || tx.type == TransactionType.BONUS
    val amountColor = if (isIncome) Color(0xFF2E7D5B) else Color(0xFFC0473C)
    val amountPrefix = if (isIncome) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tx.date.toUtcDateString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$amountPrefix${String.format("%,.2f", tx.amount)} $currency",
                style = MaterialTheme.typography.titleMedium,
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GeneralRecordCard(record: GeneralEmployeeRecord, currency: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.employeeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "عدد الأيام: ${record.attendanceDays} | آخر تحديث: ${record.lastTransactionDate.toUtcDateString()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المنظم المالي: النظام",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${String.format("%,.2f", record.totalAmount)} $currency",
                style = MaterialTheme.typography.titleMedium,
                color = RoyalNavy,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun shareViaWhatsApp(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(fallback, "مشاركة عبر"))
    }
}

fun shareExcel(context: Context, transactions: List<TransactionItem>, currency: String, empName: String) {
    try {
        val file = File(context.cacheDir, "statement_${System.currentTimeMillis()}.csv")
        val writer = file.bufferedWriter()
        writer.write('\uFEFF'.toString())
        writer.write("كشف حساب: $empName\n")
        writer.write("البيان,المبلغ ($currency),التاريخ\n")
        for (tx in transactions) {
            val prefix = if (tx.type == TransactionType.WAGE || tx.type == TransactionType.BONUS) "+" else "-"
            writer.write("${tx.description.replace(","," ")},$prefix${tx.amount},${tx.date.toUtcDateString()}\n")
        }
        writer.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "تصدير كشف الحساب"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun sharePdf(context: Context, transactions: List<TransactionItem>, currency: String, empName: String) {
    try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            isAntiAlias = true
        }

        var yPos = 50f
        canvas.drawText("كشف حساب: $empName", 200f, yPos, paint)
        yPos += 40f

        for (tx in transactions) {
            if (yPos > 800f) {
                document.finishPage(page)
                break 
            }
            val prefix = if (tx.type == TransactionType.WAGE || tx.type == TransactionType.BONUS) "+" else "-"
            val text = "${tx.description} | $prefix${tx.amount} $currency | ${tx.date.toUtcDateString()}"
            canvas.drawText(text, 50f, yPos, paint)
            yPos += 25f
        }

        document.finishPage(page)
        val file = File(context.cacheDir, "statement_${System.currentTimeMillis()}.pdf")
        val out = FileOutputStream(file)
        document.writeTo(out)
        document.close()
        out.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "تصدير كشف الحساب"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Long.toUtcDateString(): String {
    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale("en")).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }
    return format.format(java.util.Date(this))
}
