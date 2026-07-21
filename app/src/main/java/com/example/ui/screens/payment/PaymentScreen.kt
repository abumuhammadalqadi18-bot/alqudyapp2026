package com.example.ui.screens.payment

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.EmployeeEntity
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.viewmodels.EmployeeViewModel
import com.example.ui.viewmodels.FinanceViewModel
import com.example.ui.viewmodels.FinancialSummary
import com.example.ui.viewmodels.ReportViewModel
import com.example.util.safeToDouble
import com.example.util.toCurrencyFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    financeViewModel: FinanceViewModel,
    employeeViewModel: EmployeeViewModel,
    reportViewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val employeeState by employeeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currency = LocalCurrencySymbol.current

    var selectedEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showEmployeeDropdown by remember { mutableStateOf(false) }
    
    var financialSummary by remember { mutableStateOf<FinancialSummary?>(null) }
    var isLoadingSummary by remember { mutableStateOf(false) }

    var showPartialPaymentDialog by remember { mutableStateOf(false) }
    var showFullPaymentDialog by remember { mutableStateOf(false) }
    var partialAmount by remember { mutableStateOf("") }

    LaunchedEffect(selectedEmployee) {
        selectedEmployee?.let { emp ->
            isLoadingSummary = true
            financialSummary = reportViewModel.getEmployeeFinancialSummary(emp.id)
            isLoadingSummary = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "المدفوعات", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Employee Selection
            Column {
                Text(
                    text = "اختر العامل",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = showEmployeeDropdown,
                    onExpandedChange = { showEmployeeDropdown = !showEmployeeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedEmployee?.name ?: "اختر العامل من القائمة...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showEmployeeDropdown,
                        onDismissRequest = { showEmployeeDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        employeeState.activeEmployees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text(emp.name, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    selectedEmployee = emp
                                    showEmployeeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = selectedEmployee != null,
                enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (isLoadingSummary) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        financialSummary?.let { summary ->
                            // Due Balance Hero Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "الرصيد المستحق",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${summary.netPayable.toCurrencyFormat()} $currency",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Summary Cards (Wages & Advances)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SummaryCard(
                                    modifier = Modifier.weight(1f),
                                    title = "إجمالي الأجور",
                                    amount = summary.totalEarned,
                                    currency = currency,
                                    icon = Icons.Default.TrendingUp,
                                    iconColor = Color(0xFF10B981), // Green
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                                SummaryCard(
                                    modifier = Modifier.weight(1f),
                                    title = "إجمالي السلف",
                                    amount = summary.totalWithdrawn,
                                    currency = currency,
                                    icon = Icons.Default.TrendingDown,
                                    iconColor = MaterialTheme.colorScheme.error,
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            }

                            // Action Buttons
                            Text(
                                text = "الإجراءات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { showFullPaymentDialog = true },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = summary.netPayable > 0
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("دفع كامل", style = MaterialTheme.typography.titleMedium)
                                }

                                Button(
                                    onClick = { showPartialPaymentDialog = true },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    enabled = summary.netPayable > 0
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("دفع جزئي", style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { /* Print logic */ },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("طباعة سند")
                                }

                                OutlinedButton(
                                    onClick = { 
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "كشف حساب للعامل: ${selectedEmployee?.name}\nإجمالي الأجور: ${summary.totalEarned.toCurrencyFormat()} $currency\nإجمالي السلف/الدفعات: ${summary.totalWithdrawn.toCurrencyFormat()} $currency\nالرصيد المستحق: ${summary.netPayable.toCurrencyFormat()} $currency")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة كشف الحساب"))
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("مشاركة")
                                }
                            }
                            
                            OutlinedButton(
                                onClick = { /* Navigate to History */ },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("عرض سجل الدفعات")
                            }
                        }
                    }
                }
            }
        }
    }

    // Partial Payment Dialog
    if (showPartialPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPartialPaymentDialog = false },
            title = { Text("تسجيل دفعة جزئية", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل مبلغ الدفعة للعامل ${selectedEmployee?.name}:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = partialAmount,
                        onValueChange = { partialAmount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("المبلغ ($currency)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = partialAmount.safeToDouble()
                        if (amount > 0) {
                            financeViewModel.addWithdrawal(
                                employeeId = selectedEmployee!!.id,
                                amount = amount,
                                date = System.currentTimeMillis(),
                                type = com.example.domain.model.WithdrawalType.CASH,
                                description = "دفعة نقدية جزئية"
                            )
                            showPartialPaymentDialog = false
                            partialAmount = ""
                            // Refresh
                            scope.launch {
                                financialSummary = reportViewModel.getEmployeeFinancialSummary(selectedEmployee!!.id)
                            }
                        }
                    }
                ) {
                    Text("تأكيد الدفع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPartialPaymentDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Full Payment Dialog
    if (showFullPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showFullPaymentDialog = false },
            title = { Text("تصفية الحساب (دفع كامل)", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من دفع الرصيد المستحق بالكامل (${financialSummary?.netPayable?.toCurrencyFormat()} $currency) للعامل ${selectedEmployee?.name}؟") },
            confirmButton = {
                Button(
                    onClick = {
                        financialSummary?.let { summary ->
                            if (summary.netPayable > 0) {
                                financeViewModel.addWithdrawal(
                                    employeeId = selectedEmployee!!.id,
                                    amount = summary.netPayable,
                                    date = System.currentTimeMillis(),
                                    type = com.example.domain.model.WithdrawalType.CASH,
                                    description = "تصفية الحساب"
                                )
                                showFullPaymentDialog = false
                                // Refresh
                                scope.launch {
                                    financialSummary = reportViewModel.getEmployeeFinancialSummary(selectedEmployee!!.id)
                                }
                            }
                        }
                    }
                ) {
                    Text("تأكيد الدفع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullPaymentDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    currency: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${amount.toCurrencyFormat()} $currency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
