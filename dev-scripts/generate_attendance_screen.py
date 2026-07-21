content = """package com.example.ui.screens.attendance

import com.example.util.toCurrencyFormat
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.EmployeeEntity
import com.example.domain.model.DayType
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.WageViewModel
import com.example.ui.viewmodels.ReportViewModel
import com.example.ui.viewmodels.SettingsViewModel
import com.example.ui.utils.SmsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.ui.utils.toArabicFormattedDateString
import com.example.ui.utils.getUtcMidnight

data class AttendanceSelection(
    val dayType: DayType = DayType.FULL_DAY,
    val note: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    wageViewModel: WageViewModel,
    reportViewModel: ReportViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by wageViewModel.uiState.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = getUtcMidnight())
    val selectedDate = datePickerState.selectedDateMillis ?: getUtcMidnight()
    var searchQuery by remember { mutableStateOf("") }
    val currency = LocalCurrencySymbol.current
    var showSmsBanner by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val attendanceMap = remember { mutableStateMapOf<Long, AttendanceSelection>() }
    
    LaunchedEffect(uiState.activeEmployees) {
        if (attendanceMap.isEmpty() && uiState.activeEmployees.isNotEmpty()) {
            uiState.activeEmployees.forEach { employee ->
                attendanceMap[employee.id] = AttendanceSelection(DayType.FULL_DAY, "")
            }
        }
    }

    LaunchedEffect(selectedDate) {
        wageViewModel.checkAttendanceForDate(selectedDate)
    }

    val filteredEmployees = uiState.activeEmployees.filter { 
         it.name.contains(searchQuery, ignoreCase = true) || it.jobTitle.contains(searchQuery, ignoreCase = true)
    }

    val totalWage = uiState.activeEmployees.sumOf { emp ->
        if (uiState.submittedEmployeeIds.contains(emp.id)) {
            val record = uiState.recentRecords.find { it.employeeId == emp.id && it.wageDate == selectedDate }
            record?.finalAmount ?: 0.0
        } else {
            val selection = attendanceMap[emp.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
            when (selection.dayType) {
                DayType.FULL_DAY, DayType.LATE -> emp.currentDailyWage
                DayType.HALF_DAY -> emp.currentDailyWage / 2.0
                else -> 0.0
            }
        }
    }
    
    val totalCount = uiState.activeEmployees.size
    val submittedEmployeeIds = uiState.submittedEmployeeIds

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "التحضير اليومي",
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
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(RoyalNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("م", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
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
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF0F1B2B))
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي الأجور المستحقة اليوم",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${totalWage.toCurrencyFormat()} $currency",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "تم اعتماد: ${submittedEmployeeIds.size} / $totalCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                
                // Date Picker Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { showDatePicker = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AccentGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "تاريخ", tint = AccentGold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "تاريخ التحضير",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedDate.toArabicFormattedDateString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "تغيير التاريخ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    placeholder = { 
                        Text(
                            "بحث عن موظف...", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )
                )

                // Employee List
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredEmployees, key = { it.id }) { employee ->
                        val isSubmitted = submittedEmployeeIds.contains(employee.id)
                        val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                        
                        AttendanceLuxuryCard(
                            employee = employee,
                            selection = selection,
                            enabled = !isSubmitted,
                            onSelectionChange = { attendanceMap[employee.id] = it },
                            onApprove = {
                                scope.launch {
                                    val sel = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                                    wageViewModel.recordWage(
                                        employeeId = employee.id,
                                        date = selectedDate,
                                        dayType = sel.dayType,
                                        notes = sel.note.takeIf { it.isNotBlank() }
                                    )
                                    delay(500)
                                    wageViewModel.checkAttendanceForDate(selectedDate)
                                    
                                    if (settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
                                        val amount = when (sel.dayType) {
                                            DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                                            DayType.HALF_DAY -> employee.currentDailyWage / 2.0
                                            else -> 0.0
                                        }
                                        val dayTypeStr = when (sel.dayType) {
                                            DayType.FULL_DAY -> "يوم كامل"
                                            DayType.HALF_DAY -> "نصف يوم"
                                            DayType.LATE -> "متأخر"
                                            else -> ""
                                        }
                                        val netPayable = reportViewModel.getNetPayableForEmployee(employee.id)
                                        SmsHelper.sendWageSms(
                                            context = context,
                                            phone = employee.phone,
                                            employeeName = employee.name,
                                            dateMillis = selectedDate,
                                            dayTypeStr = dayTypeStr,
                                            amount = amount,
                                            currency = currency,
                                            netPayable = netPayable
                                        )
                                    } else if (!settingsState.isAutoSmsEnabled && sel.dayType != DayType.ABSENT) {
                                        showSmsBanner = true
                                    }
                                }
                            },
                            onUnapprove = {
                                wageViewModel.deleteWage(employee.id, selectedDate)
                            }
                        )
                    }
                }
            }
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
                // Logic for mass SMS has been replaced with per-employee logic as per requirements.
                // It is better to rely on auto SMS for per-employee. If they click this banner, we could send for all approved today.
            },
            onDismiss = { showSmsBanner = false }
        )
    }
}

@Composable
fun AttendanceLuxuryCard(
    employee: EmployeeEntity,
    selection: AttendanceSelection,
    enabled: Boolean,
    onSelectionChange: (AttendanceSelection) -> Unit,
    onApprove: () -> Unit,
    onUnapprove: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val indicatorColor = when {
        !enabled -> SuccessGreen
        selection.dayType == DayType.ABSENT -> DangerRed
        selection.dayType == DayType.HALF_DAY || selection.dayType == DayType.LATE -> AccentGold
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (enabled) isExpanded = !isExpanded }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .matchParentSize()
                    .align(Alignment.CenterStart)
                    .background(if (!enabled) indicatorColor else Color.Gray)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(contentAlignment = Alignment.BottomStart) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = employee.name.take(1),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (!enabled) indicatorColor else Color.Gray)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = employee.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = employee.jobTitle.ifEmpty { "غير محدد" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (!enabled) {
                        TextButton(
                            onClick = onUnapprove,
                            colors = ButtonDefaults.textButtonColors(contentColor = DangerRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("إلغاء الاعتماد", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Button(
                            onClick = { 
                                isExpanded = false
                                onApprove()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("اعتماد", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0F1B2B))
                        }
                    }
                }
                
                if (isExpanded && enabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DayTypeChip(
                                label = "يوم كامل",
                                type = DayType.FULL_DAY,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "نصف يوم",
                                type = DayType.HALF_DAY,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "متأخر",
                                type = DayType.LATE,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                            DayTypeChip(
                                label = "غائب",
                                type = DayType.ABSENT,
                                selectedType = selection.dayType,
                                onClick = { onSelectionChange(selection.copy(dayType = it)) }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = selection.note,
                            onValueChange = { onSelectionChange(selection.copy(note = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { 
                                 Text(
                                    "إضافة ملاحظة (اختياري)...", 
                                     style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                             },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayTypeChip(
    label: String,
    type: DayType,
    selectedType: DayType,
    onClick: (DayType) -> Unit
) {
    val isSelected = type == selectedType
    val bgColor = if (isSelected) AccentGold.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) AccentGold.copy(alpha = 0.5f) else Color.Transparent

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick(type) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
"""
with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "w", encoding="utf-8") as f:
    f.write(content)
