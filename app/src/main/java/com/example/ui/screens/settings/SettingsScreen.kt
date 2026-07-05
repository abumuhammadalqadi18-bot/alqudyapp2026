package com.example.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodels.SettingsViewModel
import com.example.util.DatabaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showResetDialog by remember { mutableStateOf(false) }
    var expandedCurrency by remember { mutableStateOf(false) }
    val currencies = listOf("ر.س", "د.أ", "د.إ", "ج.م")

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let {
            val success = DatabaseHelper.backupDatabase(context, it)
            if (success) {
                Toast.makeText(context, "تم النسخ الاحتياطي بنجاح!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "فشل في النسخ الاحتياطي", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val success = DatabaseHelper.restoreDatabase(context, it)
            if (success) {
                Toast.makeText(context, "تم استعادة البيانات بنجاح! يرجى إعادة تشغيل التطبيق.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "فشل في استعادة البيانات", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإعدادات",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. General Settings
            item {
                SettingsSectionTitle(title = "إعدادات النظام العامة")
            }
            item {
                SettingsCard {
                    // Currency Selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCurrency = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "العملة الافتراضية",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.currencySymbol,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Box {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "تغيير العملة",
                                tint = AccentGold
                            )
                            DropdownMenu(
                                expanded = expandedCurrency,
                                onDismissRequest = { expandedCurrency = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                currencies.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = currency,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ) 
                                        },
                                        onClick = {
                                            settingsViewModel.setCurrencySymbol(currency)
                                            expandedCurrency = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))

                    // Theme Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الوضع الداكن (Dark Mode)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تفعيل المظهر الليلي المريح للعين",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = uiState.isDarkMode,
                            onCheckedChange = { settingsViewModel.setDarkMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = RoyalNavy,
                                checkedTrackColor = AccentGold,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // 2. Data Management & Backup
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionTitle(title = "إدارة وتأمين البيانات")
            }
            item {
                SettingsCard {
                    SettingsActionRow(
                        title = "تصدير محلي",
                        subtitle = "حفظ قاعدة البيانات في الهاتف (.db)",
                        icon = Icons.Default.Save,
                        iconColor = SuccessGreen,
                        onClick = { backupLauncher.launch("alqadi_backup.db") }
                    )
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                    SettingsActionRow(
                        title = "استعادة البيانات",
                        subtitle = "استرجاع النسخة الاحتياطية",
                        icon = Icons.Default.CloudDownload,
                        iconColor = AccentGold,
                        onClick = { restoreLauncher.launch(arrayOf("application/octet-stream", "*/*")) }
                    )
                }
            }

            // 3. System Reset
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionTitle(title = "تهيئة النظام والأمان")
            }
            item {
                SettingsCard {
                    SettingsActionRow(
                        title = "مسح جميع البيانات",
                        subtitle = "حذف كافة الموظفين وسجلات التحضير نهائياً",
                        icon = Icons.Default.DeleteForever,
                        iconColor = DangerRed,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            // 4. About System
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "القاضي",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إصدار النظام 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تصميم وتطوير بأعلى معايير الجودة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "تحذير أمني",
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "هل أنت متأكد من رغبتك في مسح كافة البيانات بشكل نهائي؟ لا يمكن التراجع عن هذا الإجراء وسيتم حذف جميع سجلات الموظفين والتقارير.",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = DatabaseHelper.clearDatabase(context)
                            if (success) {
                                Toast.makeText(context, "تم مسح البيانات! يرجى إعادة تشغيل التطبيق.", Toast.LENGTH_LONG).show()
                            }
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("مسح نهائي", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("إلغاء", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = AccentGold,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
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
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .background(AccentGold)
            )
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
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
    }
}
