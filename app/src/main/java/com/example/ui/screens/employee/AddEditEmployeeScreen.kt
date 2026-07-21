package com.example.ui.screens.employee

import android.Manifest
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.EmployeeEntity
import com.example.domain.model.EmployeeStatus
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodels.EmployeeViewModel
import com.example.ui.utils.toFormattedDateString
import com.example.ui.utils.getUtcMidnight
import com.example.util.safeToDouble
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEmployeeScreen(
    employeeId: Long?,
    employeeViewModel: EmployeeViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by employeeViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val employee = remember(employeeId, uiState.activeEmployees) {
        uiState.activeEmployees.find { it.id == employeeId }
    }
    
    var name by rememberSaveable(employeeId) { mutableStateOf("") }
    var phone by rememberSaveable(employeeId) { mutableStateOf("") }
    var address by rememberSaveable(employeeId) { mutableStateOf("") }
    var jobTitle by rememberSaveable(employeeId) { mutableStateOf("") }
    var dailyWage by rememberSaveable(employeeId) { mutableStateOf("") }
    var notes by rememberSaveable(employeeId) { mutableStateOf("") }
    var photoUri by rememberSaveable(employeeId) { mutableStateOf<String?>(null) }
    var isActive by rememberSaveable(employeeId) { mutableStateOf(true) }
    var hireDate by rememberSaveable(employeeId) { mutableStateOf(getUtcMidnight()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var isInitialized by rememberSaveable(employeeId) { mutableStateOf(false) }
    
    var showSuccessAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(employee) {
        if (employee != null && !isInitialized) {
            name = employee.name
            phone = employee.phone
            address = employee.address ?: ""
            jobTitle = employee.jobTitle
            dailyWage = employee.currentDailyWage.toString()
            notes = employee.notes ?: ""
            photoUri = employee.photoPath
            isActive = employee.status == EmployeeStatus.ACTIVE
            hireDate = employee.hireDate
            isInitialized = true
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = hireDate)
    
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { hireDate = it }
    }

    val isEditing = employeeId != null
    val currency = LocalCurrencySymbol.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Must persist permission in real app, but for simplicity here we just store string
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            photoUri = it.toString()
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (idIndex >= 0 && nameIndex >= 0) {
                        val contactId = it.getString(idIndex)
                        val contactName = it.getString(nameIndex)
                        if (name.isEmpty()) name = contactName ?: ""
                        
                        val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        if (hasPhoneIndex >= 0 && it.getInt(hasPhoneIndex) > 0) {
                            val phoneCursor = context.contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )
                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (numberIndex >= 0) {
                                        phone = pc.getString(numberIndex)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Validations
    val isNameValid = name.isNotBlank()
    val isPhoneValid = phone.isNotBlank()
    val isWageValid = dailyWage.isNotBlank() && dailyWage.safeToDouble() > 0
    val isFormValid = isNameValid && isPhoneValid && isWageValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isEditing) "تعديل بيانات عامل" else "إضافة عامل جديد", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = DangerRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (isFormValid) {
                                scope.launch {
                                    showSuccessAnimation = true
                                    delay(600) // Let animation play
                                    val newEmployee = EmployeeEntity(
                                        id = employeeId ?: 0,
                                        name = name,
                                        phone = phone,
                                        jobTitle = jobTitle,
                                        nationalId = null,
                                        address = address.takeIf { it.isNotBlank() },
                                        photoPath = photoUri,
                                        hireDate = hireDate,
                                        currentDailyWage = dailyWage.safeToDouble(),
                                        status = if (isActive) EmployeeStatus.ACTIVE else EmployeeStatus.INACTIVE,
                                        notes = notes,
                                        createdAt = employee?.createdAt ?: System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                    if (isEditing) {
                                        employeeViewModel.updateEmployee(newEmployee)
                                    } else {
                                        employeeViewModel.addEmployee(newEmployee)
                                    }
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        if (showSuccessAnimation) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(28.dp))
                        } else {
                            Text(
                                "حفظ البيانات",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Photo picker
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(Uri.parse(photoUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "صورة العامل",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "اختر صورة",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            item {
                CustomTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "اسم العامل *",
                    icon = Icons.Outlined.Person,
                    isError = !isNameValid && name.isNotEmpty(),
                    supportingText = if (!isNameValid && name.isNotEmpty()) "اسم العامل مطلوب" else null,
                    trailingIcon = {
                        IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                            Icon(Icons.Default.Contacts, contentDescription = "استيراد من جهات الاتصال", tint = AccentGold)
                        }
                    }
                )
            }

            item {
                CustomTextField(
                    value = phone,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '+' }) phone = it },
                    label = "رقم الهاتف *",
                    icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone,
                    isError = !isPhoneValid && phone.isNotEmpty(),
                    supportingText = if (!isPhoneValid && phone.isNotEmpty()) "رقم الهاتف مطلوب" else null
                )
            }

            item {
                CustomTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "العنوان",
                    icon = Icons.Outlined.LocationOn
                )
            }

            item {
                CustomTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = "المهنة / الوظيفة",
                    icon = Icons.Outlined.Work
                )
            }

            item {
                CustomTextField(
                    value = dailyWage,
                    onValueChange = { newValue ->
                        val filtered = newValue.replace(",", ".").replace("،", ".").replace("٫", ".")
                        if (filtered.count { it == '.' } <= 1 && filtered.all { it.isDigit() || it == '.' }) {
                            dailyWage = filtered
                        }
                    },
                    label = "الأجر اليومي *",
                    icon = Icons.Outlined.AttachMoney,
                    keyboardType = KeyboardType.Decimal,
                    suffix = currency,
                    isError = !isWageValid && dailyWage.isNotEmpty(),
                    supportingText = if (!isWageValid && dailyWage.isNotEmpty()) "يجب إدخال مبلغ صحيح أكبر من صفر" else null
                )
            }

            item {
                OutlinedTextField(
                    value = hireDate.toFormattedDateString(),
                    onValueChange = { },
                    label = { Text("تاريخ الانضمام") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    enabled = false,
                    leadingIcon = {
                        Icon(Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "حالة العامل",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isActive) "نشط (على رأس العمل)" else "غير نشط (موقوف/مستقيل)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryTeal,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Notes, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryTeal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
        
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("موافق", color = PrimaryTeal)
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

        if (showDeleteDialog && employee != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "تنبيه حرج",
                        style = MaterialTheme.typography.titleLarge,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "هل أنت متأكد من حذف هذا العامل نهائياً؟ سيؤدي هذا إلى حذف جميع سجلات حضوره وسحوباته المالية المرتبطة به فوراً ولا يمكن التراجع عن هذا الإجراء.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            employeeViewModel.archiveEmployee(employee.id)
                            onNavigateBack()
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("حذف نهائي", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("إلغاء", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = trailingIcon,
        suffix = { if (suffix != null) Text(suffix) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        supportingText = if (isError && supportingText != null) {
            { Text(supportingText, color = DangerRed) }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryTeal,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            errorBorderColor = DangerRed,
            errorLabelColor = DangerRed,
            errorLeadingIconColor = DangerRed
        ),
        singleLine = true
    )
}
