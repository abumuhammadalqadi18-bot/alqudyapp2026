package com.example.ui.screens.attendance

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

    // Map of employeeId -> AttendanceSelection
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

    var isSaving by remember { mutableStateOf(false) }

    // Calculate totals and filter
    val filteredEmployees = uiState.activeEmployees.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.jobTitle.contains(searchQuery, ignoreCase = true)
    }

    val totalWage = uiState.activeEmployees.sumOf { emp ->
        val selection = attendanceMap[emp.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
        when (selection.dayType) {
            DayType.FULL_DAY, DayType.LATE -> emp.currentDailyWage
            DayType.HALF_DAY -> emp.currentDailyWage / 2.0
            else -> 0.0
        }
    }
    
    val presentCount = attendanceMap.count { it.value.dayType != DayType.ABSENT }
    val absentCount = attendanceMap.count { it.value.dayType == DayType.ABSENT }
    val totalCount = attendanceMap.size

    val submittedEmployeeIds = uiState.submittedEmployeeIds
    val allSubmitted = uiState.activeEmployees.isNotEmpty() && uiState.activeEmployees.all { submittedEmployeeIds.contains(it.id) }

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
            // Dark luxury bottom container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF0F1B2B)) // Always dark navy regardless of theme
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right Side: Total Text (RTL means this is on the right visually)
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
                    
                    // Left Side: Button
                    Button(
                        onClick = {
                            if (!isSaving && !allSubmitted) {
                                scope.launch {
                                    isSaving = true
                                    val sentEmployees = mutableListOf<EmployeeEntity>()
                                    
                                    uiState.activeEmployees.forEach { employee ->
                                        val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                                        wageViewModel.recordWage(
                                            employeeId = employee.id,
                                            date = selectedDate,
                                            dayType = selection.dayType,
                                            notes = selection.note.takeIf { it.isNotBlank() }
                                        )
                                        sentEmployees.add(employee)
                                    }
                                    delay(800)
                                    isSaving = false
                                    if (!settingsState.isAutoSmsEnabled) {
                                        showSmsBanner = true
                                    }
                                    wageViewModel.checkAttendanceForDate(selectedDate)
                                    
                                    if (settingsState.isAutoSmsEnabled) {
                                        sentEmployees.forEach { employee ->
                                            val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                                            if (selection.dayType != DayType.ABSENT) {
                                                val amount = when (selection.dayType) {
                                                    DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                                                    DayType.HALF_DAY -> employee.currentDailyWage / 2.0
                                                    else -> 0.0
                                                }
                                                val dayTypeStr = when (selection.dayType) {
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
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (allSubmitted) Color.Gray else AccentGold,
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        enabled = !allSubmitted
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFF0F1B2B),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (allSubmitted) "معتمد\nمسبقاً" else "اعتماد\nالتحضير",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (allSubmitted) Color.White else Color(0xFF0F1B2B),
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircleOutline,
                                    contentDescription = "اعتماد",
                                    tint = if (allSubmitted) Color.White else Color(0xFF0F1B2B)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
            // 1. Top Header Card (Date & Stats)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تاريخ التحضير",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedDate.toArabicFormattedDateString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "تغيير التاريخ",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AccentGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "التاريخ",
                                tint = AccentGold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "حضور", value = presentCount.toString(), color = SuccessGreen)
                        StatItem(label = "غياب", value = absentCount.toString(), color = DangerRed)
                        StatItem(label = "الإجمالي", value = totalCount.toString(), color = AccentGold)
                    }
                }
            }

            // 2. Search & Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Icon Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .clickable { /* Filter Action */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "فلتر",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Search Field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "ابحث عن اسم الموظف...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            

            // 3. Employees List
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentGold)
                }
            } else if (filteredEmployees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "لا توجد نتائج.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 120.dp) // Leave space for bottom container
                ) {
                    items(filteredEmployees, key = { it.id }) { employee ->
                        val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                        AttendanceLuxuryCard(
                            employee = employee,
                            selection = selection,
                            onSelectionChange = { attendanceMap[employee.id] = it },
                            enabled = !allSubmitted,
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
                scope.launch {
                    val sentEmployees = uiState.activeEmployees
                    sentEmployees.forEach { employee ->
                        val selection = attendanceMap[employee.id] ?: AttendanceSelection(DayType.FULL_DAY, "")
                    if (selection.dayType != DayType.ABSENT) {
                        val amount = when (selection.dayType) {
                            DayType.FULL_DAY, DayType.LATE -> employee.currentDailyWage
                            DayType.HALF_DAY -> employee.currentDailyWage / 2.0
                            else -> 0.0
                        }
                        val dayTypeStr = when (selection.dayType) {
                            DayType.FULL_DAY -> "يوم كامل"
                            DayType.HALF_DAY -> "نصف يوم"
                            DayType.LATE -> "متأخر"
                            else -> ""
                        }
                        val netPayable = reportViewModel.getNetPayableForEmployee(employee.id)
                        com.example.ui.utils.SmsHelper.sendWageSms(
                            context = context,
                            phone = employee.phone,
                            employeeName = employee.name,
                            dateMillis = selectedDate,
                            dayTypeStr = dayTypeStr,
                            amount = amount,
                            currency = currency,
                            netPayable = netPayable
                        )
                    }
                }
                }
            },
            onDismiss = {
                showSmsBanner = false
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AttendanceLuxuryCard(
    employee: EmployeeEntity,
    selection: AttendanceSelection,
    onSelectionChange: (AttendanceSelection) -> Unit,
    enabled: Boolean,
    onUnapprove: () -> Unit = {}
) {
    val indicatorColor = when (selection.dayType) {
        DayType.FULL_DAY -> SuccessGreen
        DayType.HALF_DAY -> Color(0xFF4DB6AC) // Teal
        DayType.LATE -> AccentGold
        DayType.ABSENT -> DangerRed
        else -> MaterialTheme.colorScheme.outline
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = AccentGold),
                onClick = { if (enabled) isExpanded = !isExpanded }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            // Right colored edge (Start side in RTL)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .matchParentSize()
                    .align(Alignment.CenterStart)
                    .background(if (enabled) indicatorColor else Color.Gray)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // Profile & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        // Profile Image with Indicator
                        Box(contentAlignment = Alignment.BottomStart) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
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
                            // Status Dot
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(if (enabled) indicatorColor else Color.Gray)
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
                    
                    // Status Badge & Unapprove Button
                    if (!enabled) {
                        androidx.compose.material3.TextButton(
                            onClick = onUnapprove,
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = DangerRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("إلغاء الاعتماد", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(indicatorColor.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (selection.dayType) {
                                    DayType.FULL_DAY -> "يوم كامل"
                                    DayType.HALF_DAY -> "نصف يوم"
                                    DayType.LATE -> "متأخر"
                                    DayType.ABSENT -> "غائب"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = indicatorColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Expanded Details: Selection Chips and Note
                if (isExpanded && enabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Chips
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
                        
                        // Note Input
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
