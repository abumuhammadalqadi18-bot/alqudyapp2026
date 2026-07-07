package com.example.ui.screens.settings

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.RoyalNavy
import com.example.ui.viewmodels.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showPinDialog by remember { mutableStateOf(false) }
    var tempPin by remember { mutableStateOf("") }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            settingsViewModel.clearActionMessage()
        }
    }

    val localBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            settingsViewModel.backupToLocal(context, it)
        }
    }

    val localRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            settingsViewModel.restoreFromLocal(context, it)
        }
    }

    val googleSignInBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            settingsViewModel.backupToCloud(context)
        } else {
            Toast.makeText(context, "تم إلغاء عملية الربط بحساب Google", Toast.LENGTH_SHORT).show()
        }
    }

    val googleSignInRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            settingsViewModel.restoreFromCloud(context)
        } else {
            Toast.makeText(context, "تم إلغاء عملية الربط بحساب Google", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchGoogleAccountChooser(launcher: androidx.activity.compose.ManagedActivityResultLauncher<Intent, androidx.activity.result.ActivityResult>) {
        val intent = android.accounts.AccountManager.newChooseAccountIntent(
            null, null, arrayOf("com.google"), null, null, null, null
        )
        launcher.launch(intent)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "الإعدادات العامة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. App Style
            item {
                SettingsSectionTitle("المظهر والعملة")
                SettingsCard {
                    // Theme
                    var themeExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { themeExpanded = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("وضع المظهر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            val themeName = when (uiState.themeMode) {
                                "LIGHT" -> "الوضع الفاتح ☀️"
                                "DARK" -> "الوضع الداكن 🌙"
                                else -> "الافتراضي للنظام"
                            }
                            Text(themeName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box {
                            DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("الافتراضي للنظام") },
                                    onClick = { settingsViewModel.setThemeMode("SYSTEM"); themeExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("الوضع الفاتح ☀️") },
                                    onClick = { settingsViewModel.setThemeMode("LIGHT"); themeExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("الوضع الداكن 🌙") },
                                    onClick = { settingsViewModel.setThemeMode("DARK"); themeExpanded = false }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Currency
                    var currencyExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currencyExpanded = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("العملة الافتراضية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(uiState.currencySymbol, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box {
                            DropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                                listOf("ريال يمني", "دولار أمريكي", "ريال سعودي", "درهم إماراتي", "جنيه مصري", "دينار أردني").forEach { curr ->
                                    DropdownMenuItem(
                                        text = { Text(curr) },
                                        onClick = { settingsViewModel.setCurrencySymbol(curr); currencyExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. SMS Notifications
            item {
                SettingsSectionTitle("تنبيهات وإشعارات SMS")
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsViewModel.setAutoSmsEnabled(!uiState.isAutoSmsEnabled) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إرسال رسائل SMS تلقائية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("إشعار الموظفين فور تسجيل التحضير أو الحركات المالية", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.isAutoSmsEnabled,
                            onCheckedChange = { settingsViewModel.setAutoSmsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = RoyalNavy,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // 3. Security & Biometrics
            item {
                SettingsSectionTitle("الأمان والخصوصية")
                SettingsCard {
                    // PIN Lock
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (uiState.isPinLockEnabled) {
                                    settingsViewModel.setPinLockEnabled(false)
                                    settingsViewModel.setPinCode("")
                                } else {
                                    showPinDialog = true
                                }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("قفل التطبيق برمز PIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("طلب رمز مكون من 4 أرقام عند فتح التطبيق", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.isPinLockEnabled,
                            onCheckedChange = {
                                if (it) {
                                    showPinDialog = true
                                } else {
                                    settingsViewModel.setPinLockEnabled(false)
                                    settingsViewModel.setPinCode("")
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D5B),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Biometric Lock
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settingsViewModel.setBiometricEnabled(!uiState.isBiometricEnabled) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تسجيل الدخول بالبصمة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("استخدام البصمة الحيوية لحماية السجلات", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { settingsViewModel.setBiometricEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D5B),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // 4. Backup & Restore
            item {
                SettingsSectionTitle("النسخ الاحتياطي والاستعادة")
                SettingsCard {
                    BackupActionRow(
                        title = "نسخ احتياطي محلي للهاتف",
                        subtitle = "حفظ البيانات كملف (.db) في ذاكرة الهاتف",
                        icon = Icons.Default.Save,
                        iconColor = RoyalNavy,
                        isLoading = uiState.isLocalSaving,
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
                            localBackupLauncher.launch("AlQadhi_Backup_$timeStamp.db")
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    BackupActionRow(
                        title = "استيراد البيانات من الهاتف",
                        subtitle = "استعادة البيانات من ملف محلي سابق",
                        icon = Icons.Default.SettingsBackupRestore,
                        iconColor = Color(0xFFE67E22),
                        isLoading = uiState.isLocalRestoring,
                        onClick = {
                            localRestoreLauncher.launch(arrayOf("*/*"))
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    BackupActionRow(
                        title = "ربط ونسخ عبر Google Drive",
                        subtitle = "حفظ البيانات سحابياً عبر حساب Google",
                        icon = Icons.Default.CloudUpload,
                        iconColor = Color(0xFF2E7D5B),
                        isLoading = uiState.isCloudSaving,
                        onClick = {
                            launchGoogleAccountChooser(googleSignInBackupLauncher)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    BackupActionRow(
                        title = "استيراد البيانات من السحابة",
                        subtitle = "استعادة النسخة السحابية من Google Drive",
                        icon = Icons.Default.CloudDownload,
                        iconColor = Color(0xFFC0473C),
                        isLoading = uiState.isCloudRestoring,
                        onClick = {
                            launchGoogleAccountChooser(googleSignInRestoreLauncher)
                        }
                    )
                }
            }

            // 5. About App
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(RoyalNavy),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚖️", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "القاضي لإدارة الأجور",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "نظام متكامل ومحمي لإدارة أجور وعمال المشاريع. مرخص مالياً ومعتمد بأعلى معايير الجودة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showPinDialog) {
        Dialog(onDismissRequest = { 
            showPinDialog = false
            tempPin = ""
        }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Password, contentDescription = null, tint = RoyalNavy, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("تعيين رمز القفل (PIN)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("أدخل رمزاً مكوناً من 4 أرقام", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) tempPin = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, letterSpacing = 8.sp),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { showPinDialog = false; tempPin = "" }) {
                            Text("إلغاء", color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = {
                                if (tempPin.length == 4) {
                                    settingsViewModel.setPinCode(tempPin)
                                    settingsViewModel.setPinLockEnabled(true)
                                    showPinDialog = false
                                    tempPin = ""
                                    Toast.makeText(context, "تم تفعيل القفل بنجاح", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "الرجاء إدخال 4 أرقام", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy)
                        ) {
                            Text("تأكيد وحفظ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = RoyalNavy,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun BackupActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = iconColor, strokeWidth = 2.dp)
        }
    }
}
