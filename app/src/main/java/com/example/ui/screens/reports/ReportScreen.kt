package com.example.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ui.theme.AccentGold
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.viewmodels.ReportViewModel
import com.example.ui.viewmodels.TransactionItem
import com.example.ui.viewmodels.TransactionType
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("DEPRECATION")
@Composable
fun ReportScreen(
    reportViewModel: ReportViewModel
) {
    val reportState by reportViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currency = LocalCurrencySymbol.current

    // Filters
    var selectedFilter by remember { mutableStateOf("يومي") }
    val filters = listOf("يومي", "أسبوعي", "شهري", "كل المدة")

    var selectedEmployeeId by remember { mutableStateOf<Long?>(null) }
    var showEmployeeDropdown by remember { mutableStateOf(false) }

    val employees = remember(reportState.summaries) {
        reportState.summaries.map { it.employeeName to it.employeeId }.distinctBy { it.second }
    }

    LaunchedEffect(Unit) {
        reportViewModel.loadReports(selectedFilter)
    }

    LaunchedEffect(selectedFilter) {
        reportViewModel.loadReports(selectedFilter)
    }

    val filteredTransactions = remember(reportState.transactions, selectedEmployeeId) {
        if (selectedEmployeeId == null) {
            reportState.transactions
        } else {
            reportState.transactions.filter { it.employeeId == selectedEmployeeId }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تقارير الأجور والسحوبات",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RoyalNavy)
                    .padding(16.dp)
                    .padding(bottom = 56.dp) // padding for app bottom bar
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { sharePdf(context, filteredTransactions, currency) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تصدير PDF 📄", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { shareExcel(context, filteredTransactions, currency) },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تصدير Excel 📊", color = RoyalNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Row (Time Period)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentGold else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) RoyalNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Employee Dropdown
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = showEmployeeDropdown,
                    onExpandedChange = { showEmployeeDropdown = !showEmployeeDropdown }
                ) {
                    val selectedName = employees.find { it.second == selectedEmployeeId }?.first ?: "جميع الموظفين"
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
                    )
                    ExposedDropdownMenu(
                        expanded = showEmployeeDropdown,
                        onDismissRequest = { showEmployeeDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("جميع الموظفين") },
                            onClick = {
                                selectedEmployeeId = null
                                showEmployeeDropdown = false
                            }
                        )
                        employees.forEach { (name, id) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedEmployeeId = id
                                    showEmployeeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reportState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGold)
                }
            } else if (filteredTransactions.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.List,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا توجد سجلات للفترة المحددة",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionCard(tx, currency)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(tx: TransactionItem, currency: String) {
    val isIncome = tx.type == TransactionType.WAGE || tx.type == TransactionType.BONUS
    val amountColor = if (isIncome) Color(0xFF2E7D5B) else Color(0xFFC0473C)
    val amountPrefix = if (isIncome) "+" else "-"
    val title = when (tx.type) {
        TransactionType.WAGE -> "أجر حضور"
        TransactionType.WITHDRAWAL -> "سحب مالي"
        TransactionType.BONUS -> "مكافأة"
        TransactionType.DEDUCTION -> "خصم"
    }

    val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.employeeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ${dateFormatter.format(Date(tx.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (tx.description.isNotBlank() && tx.description != "حضور" && tx.description != "سحب") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tx.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
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

fun shareExcel(context: Context, transactions: List<TransactionItem>, currency: String) {
    try {
        val file = File(context.cacheDir, "reports.csv")
        val writer = file.bufferedWriter()
        writer.write('\uFEFF'.toString()) // BOM for Excel UTF-8
        writer.write("اسم الموظف,النوع,المبلغ ($currency),التاريخ,الملاحظات\n")
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar"))
        for (tx in transactions) {
            val typeStr = when (tx.type) {
                TransactionType.WAGE -> "أجر"
                TransactionType.WITHDRAWAL -> "سحب"
                TransactionType.BONUS -> "مكافأة"
                TransactionType.DEDUCTION -> "خصم"
            }
            writer.write("${tx.employeeName},$typeStr,${tx.amount},${dateFormatter.format(Date(tx.date))},${tx.description.replace(","," ")}\n")
        }
        writer.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير (Excel)"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun sharePdf(context: Context, transactions: List<TransactionItem>, currency: String) {
    try {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            isAntiAlias = true
        }

        var yPos = 50f
        canvas.drawText("تقرير الأجور والسحوبات", 200f, yPos, paint)
        yPos += 40f

        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("ar"))
        for (tx in transactions) {
            if (yPos > 800f) {
                document.finishPage(page)
                break 
            }
            val typeStr = when (tx.type) {
                TransactionType.WAGE -> "أجر"
                TransactionType.WITHDRAWAL -> "سحب"
                TransactionType.BONUS -> "مكافأة"
                TransactionType.DEDUCTION -> "خصم"
            }
            val text = "${tx.employeeName} | $typeStr | ${tx.amount} $currency | ${dateFormatter.format(Date(tx.date))}"
            canvas.drawText(text, 50f, yPos, paint)
            yPos += 25f
        }

        document.finishPage(page)
        val file = File(context.cacheDir, "reports.pdf")
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
        context.startActivity(Intent.createChooser(intent, "مشاركة التقرير (PDF)"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
