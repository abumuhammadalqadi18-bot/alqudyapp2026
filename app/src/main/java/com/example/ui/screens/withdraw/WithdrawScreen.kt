package com.example.ui.screens.withdraw

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SmsBanner
import com.example.data.local.entity.EmployeeEntity
import com.example.domain.model.WithdrawalType
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.EmployeeViewModel
import com.example.ui.viewmodels.FinanceViewModel
import com.example.ui.viewmodels.ReportViewModel
import com.example.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.utils.toArabicFormattedDateString
import com.example.ui.utils.getUtcMidnight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    financeViewModel: FinanceViewModel,
    employeeViewModel: EmployeeViewModel,
    settingsViewModel: SettingsViewModel,
    reportViewModel: ReportViewModel,
    onNavigateBack: () -> Unit
) {
    val employeeState by employeeViewModel.uiState.collectAsState()
    val financeState by financeViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val currency = LocalCurrencySymbol.current

    var selectedEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var showEmployeeDropdown by remember { mutableStateOf(false) }

    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull() ?: 0.0

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = getUtcMidnight())
    val selectedDate = datePickerState.selectedDateMillis ?: getUtcMidnight()

    var isCash by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    var netPayable by remember { mutableStateOf(0.0) }
    var showSmsBanner by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var savedWithdrawal by remember { mutableStateOf(false) }

    LaunchedEffect(selectedEmployee) {
        selectedEmployee?.let {
            netPayable = reportViewModel.getNetPayableForEmployee(it.id)
        }
    }

    val exceedsNet = selectedEmployee != null && amount > netPayable

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "تسجيل سحب جديد",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedEmployee != null && amount > 0) {
                            scope.launch {
                                financeViewModel.addWithdrawal(
                                    employeeId = selectedEmployee!!.id,
                                    amount = amount,
                                    date = selectedDate,
                                    type = if (isCash) WithdrawalType.CASH else WithdrawalType.DEFERRED,
                                    description = notes.takeIf { it.isNotBlank() }
                                )
                                delay(300)
                                savedWithdrawal = true
                                if (settingsState.isAutoSmsEnabled) {
                                    com.example.ui.utils.SmsHelper.sendWithdrawalSms(
                                        context = context,
                                        phone = selectedEmployee!!.phone,
                                        employeeName = selectedEmployee!!.name,
                                        dateMillis = selectedDate,
                                        isCash = isCash,
                                        amount = amount,
                                        currency = currency,
                                        notes = notes,
                                        netPayable = netPayable - amount
                                    )
                                    onNavigateBack()
                                } else {
                                    showSmsBanner = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedEmployee != null && amount > 0 && !financeState.isLoading && !savedWithdrawal
                ) {
                    if (financeState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF0F1B2B),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "حفظ السحب",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0F1B2B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Employee Dropdown
            item {
                Text(
                    text = "الموظف",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = showEmployeeDropdown,
                    onExpandedChange = { showEmployeeDropdown = !showEmployeeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedEmployee?.name ?: "اختر الموظف...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEmployeeDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                    ExposedDropdownMenu(
                        expanded = showEmployeeDropdown,
                        onDismissRequest = { showEmployeeDropdown = false }
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

            // 2. Amount Field
            item {
                Text(
                    text = "مبلغ السحب",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    singleLine = true,
                    trailingIcon = {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                )

                // Warning banner if amount exceeds net payable
                AnimatedVisibility(visible = exceedsNet) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF39C12).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFF39C12).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "تحذير",
                            tint = Color(0xFFF39C12)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "مبلغ السحب يتجاوز الصافي المستحق للموظف!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF39C12),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الصافي الحالي: ${String.format("%,.2f", netPayable)} $currency",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Date Picker
            item {
                Text(
                    text = "تاريخ السحب",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = selectedDate.toArabicFormattedDateString(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "اختر التاريخ",
                            tint = AccentGold
                        )
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTrailingIconColor = AccentGold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }

            // 4. Withdrawal Type (Segmented Button)
            item {
                Text(
                    text = "نوع السحب",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCash) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isCash = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "نقدي 💵",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isCash) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isCash) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isCash) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isCash = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "آجل ⏱️",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (!isCash) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (!isCash) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // 5. Notes
            item {
                Text(
                    text = "ملاحظات (اختياري)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            "اكتب أي ملاحظات إضافية هنا...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // Padding for bottom bar
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("موافق", color = AccentGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("إلغاء", color = DangerRed)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        
        com.example.ui.components.SmsBanner(
            isVisible = showSmsBanner,
            onSend = {
                showSmsBanner = false
                com.example.ui.utils.SmsHelper.sendWithdrawalSms(
                    context = context,
                    phone = selectedEmployee!!.phone,
                    employeeName = selectedEmployee!!.name,
                    dateMillis = selectedDate,
                    isCash = isCash,
                    amount = amount,
                    currency = currency,
                    notes = notes,
                    netPayable = netPayable - amount
                )
                onNavigateBack()
            },
            onDismiss = {
                showSmsBanner = false
                onNavigateBack()
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        }
    }
}


