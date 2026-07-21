package com.example.ui.screens.settings
import com.example.ui.theme.AccentGold

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.viewmodels.SettingsViewModel
import com.example.ui.viewmodels.SettingsUiState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            settingsViewModel.clearActionMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                SettingsHeader()
            }
            
            item {
                CompanyProfileSection(settingsViewModel, uiState)
            }
            
            item {
                GeneralSettingsSection(settingsViewModel, uiState)
            }
            
            item {
                SecuritySettingsSection(settingsViewModel, uiState)
            }
            
            item {
                BackupSettingsSection(settingsViewModel, uiState)
            }
        }
    }
}

@Composable
fun SettingsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "الإعدادات",
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "إعدادات النظام",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "تخصيص تفضيلات التطبيق والوثائق الرسمية",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun CompanyProfileSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    var expanded by remember { mutableStateOf(true) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    var companyName by remember(uiState.companyName) { mutableStateOf(uiState.companyName) }
    var phoneNumbers by remember(uiState.phoneNumbers) { mutableStateOf(uiState.phoneNumbers) }
    var address by remember(uiState.address) { mutableStateOf(uiState.address) }
    var services by remember(uiState.services) { mutableStateOf(uiState.services) }
    var footerNote by remember(uiState.footerNote) { mutableStateOf(uiState.footerNote) }
    
    var logoUri by remember(uiState.logoUri) { mutableStateOf(uiState.logoUri?.let { Uri.parse(it) }) }
    var sealUri by remember(uiState.sealUri) { mutableStateOf(uiState.sealUri?.let { Uri.parse(it) }) }
    var signatureUri by remember(uiState.signatureUri) { mutableStateOf(uiState.signatureUri?.let { Uri.parse(it) }) }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { logoUri = it } }
    val sealPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { sealUri = it } }
    val signaturePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { signatureUri = it } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .border(1.dp, AccentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "إعدادات هوية المؤسسة والوثائق الرسمية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "تخصيص بيانات الترويسة، الأختام، والتوقيع",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotationState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("اسم المؤسسة (يظهر في ترويسة التقارير)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phoneNumbers,
                        onValueChange = { phoneNumbers = it },
                        label = { Text("أرقام التواصل") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان الجغرافي") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = services,
                        onValueChange = { services = it },
                        label = { Text("الخدمات أو المنتجات الرئيسية") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = footerNote,
                        onValueChange = { footerNote = it },
                        label = { Text("الملاحظة الختامية (في أسفل السندات)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = AccentGold.copy(alpha = 0.5f))

                    Text(
                        text = "الأختام والتواقيع الرسمية",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    ImagePickerRow(
                        title = "شعار المؤسسة (Logo)",
                        description = "يظهر في أعلى يسار التقارير",
                        imageUri = logoUri,
                        isCircular = true,
                        onClick = { logoPicker.launch("image/*") }
                    )

                    ImagePickerRow(
                        title = "الختم الرسمي",
                        description = "يظهر أسفل التقارير بجانب التوقيع",
                        imageUri = sealUri,
                        isCircular = true,
                        onClick = { sealPicker.launch("image/*") }
                    )

                    ImagePickerRow(
                        title = "توقيع المدير العام",
                        description = "يفضل صورة شفافة (PNG)",
                        imageUri = signatureUri,
                        isCircular = false,
                        onClick = { signaturePicker.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            settingsViewModel.saveCompanyProfile(
                                companyName, phoneNumbers, address, services, footerNote, logoUri, sealUri, signatureUri
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("حفظ إعدادات الهوية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ImagePickerRow(title: String, description: String, imageUri: Uri?, isCircular: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (isCircular) 64.dp else 80.dp)
                        .padding(end = 12.dp)
                        .clip(if (isCircular) CircleShape else RoundedCornerShape(8.dp))
                        .border(BorderStroke(2.dp, AccentGold), if (isCircular) CircleShape else RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { onClick() },
                    contentScale = ContentScale.Crop
                )
            }
            FilledTonalButton(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(if (imageUri == null) Icons.Default.AddPhotoAlternate else Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                Text(if (imageUri == null) "اختيار" else "تغيير")
            }
        }
    }
}

@Composable
fun GeneralSettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    SettingsCard(title = "الإعدادات العامة", icon = Icons.Default.Language) {
        SettingSwitchRow(
            title = "الوضع الليلي",
            description = "تفعيل المظهر الداكن للتطبيق",
            checked = uiState.isDarkMode,
            onCheckedChange = { settingsViewModel.setDarkMode(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingSwitchRow(
            title = "تنبيهات SMS",
            description = "إرسال إشعار للموظف عند تسجيل حركة",
            checked = uiState.isAutoSmsEnabled,
            onCheckedChange = { settingsViewModel.setAutoSmsEnabled(it) }
        )
    }
}

@Composable
fun SecuritySettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                tempPin = ""
                pinError = null
            },
            title = { Text("إعداد رمز PIN (4 أرقام)") },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { 
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                tempPin = it
                                pinError = null
                            }
                        },
                        label = { Text("رمز PIN") },
                        isError = pinError != null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    if (pinError != null) {
                        Text(text = pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempPin.length == 4) {
                        settingsViewModel.setPinCode(tempPin)
                        settingsViewModel.setPinLockEnabled(true)
                        showPinDialog = false
                        tempPin = ""
                    } else {
                        pinError = "يجب أن يتكون الرمز من 4 أرقام"
                    }
                }) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    tempPin = ""
                    pinError = null
                }) {
                    Text("إلغاء")
                }
            }
        )
    }

    SettingsCard(title = "الحماية والخصوصية", icon = Icons.Default.Security) {
        SettingSwitchRow(
            title = "الدخول بالبصمة",
            description = "تأمين التطبيق باستخدام البصمة الحيوية",
            checked = uiState.isBiometricEnabled,
            onCheckedChange = { settingsViewModel.setBiometricEnabled(it) }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingSwitchRow(
            title = "قفل برمز PIN",
            description = "تأمين التطبيق برمز مرور",
            checked = uiState.isPinLockEnabled,
            onCheckedChange = {
                if (it) {
                    showPinDialog = true
                } else {
                    settingsViewModel.setPinLockEnabled(false)
                }
            }
        )
        if (uiState.isPinLockEnabled) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
            SettingActionRow(
                title = "تغيير رمز PIN",
                description = "تحديث رمز المرور الخاص بك",
                buttonText = "تغيير",
                onClick = { showPinDialog = true }
            )
        }
    }
}

@Composable
fun BackupSettingsSection(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    val context = LocalContext.current
    
    val createLocalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) settingsViewModel.backupToLocal(context, uri)
    }
    val openLocalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) settingsViewModel.restoreFromLocal(context, uri)
    }
    
    SettingsCard(title = "النسخ الاحتياطي", icon = Icons.Default.CloudSync) {
        SettingActionRow(
            title = "تصدير نسخة احتياطية",
            description = "حفظ نسخة من البيانات (محلي / سحابي عبر مدير الملفات)",
            buttonText = "تصدير",
            onClick = { createLocalLauncher.launch("alqadi_backup.db") }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        SettingActionRow(
            title = "استيراد نسخة احتياطية",
            description = "استعادة البيانات من ملف النسخة الاحتياطية",
            buttonText = "استيراد",
            onClick = { openLocalLauncher.launch(arrayOf("*/*")) }
        )
    }
}

@Composable
fun SettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
fun SettingSwitchRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun SettingActionRow(title: String, description: String, buttonText: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = onClick) {
            Text(buttonText)
        }
    }
}

