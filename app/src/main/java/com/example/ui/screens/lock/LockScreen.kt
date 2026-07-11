package com.example.ui.screens.lock

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DangerRed
import com.example.ui.viewmodels.SettingsViewModel

@Composable
fun LockScreen(
    settingsViewModel: SettingsViewModel,
    onUnlockSuccess: () -> Unit
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pinCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    

    val showBiometricPrompt = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        errorMessage = "خطأ في المصادقة: $errString"
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onUnlockSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        errorMessage = "فشلت المصادقة"
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("تسجيل الدخول للتطبيق")
                .setSubtitle("قم باستخدام البصمة لفتح التطبيق")
                .setNegativeButtonText("استخدام الرمز السري")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(context, "النشاط غير مدعوم للمصادقة الحيوية", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.isBiometricEnabled) {
        if (uiState.isBiometricEnabled) {
            showBiometricPrompt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "قفل التطبيق",
            tint = AccentGold,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "أدخل الرمز السري",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))

        // PIN Indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < pinCode.length) AccentGold else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                )
                if (i < 3) Spacer(modifier = Modifier.width(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = DangerRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "del")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp))
                    } else if (key == "del") {
                        IconButton(
                            onClick = {
                                if (pinCode.isNotEmpty()) {
                                    pinCode = pinCode.dropLast(1)
                                    errorMessage = null
                                }
                            },
                            modifier = Modifier.size(72.dp)
                        ) {
                            @Suppress("DEPRECATION")
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "مسح",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        TextButton(
                            onClick = {
                                if (pinCode.length < 4) {
                                    pinCode += key
                                    errorMessage = null
                                    if (pinCode.length == 4) {
                                        if (pinCode == uiState.pinCode) {
                                            onUnlockSuccess()
                                        } else {
                                            errorMessage = "الرمز السري غير صحيح"
                                            pinCode = ""
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (uiState.isBiometricEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showBiometricPrompt() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AccentGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("استخدام البصمة", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
