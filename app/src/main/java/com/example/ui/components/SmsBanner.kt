package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.theme.AccentGold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.delay

@Composable
fun SmsBanner(
    isVisible: Boolean,
    onSend: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible) {
        if (isVisible) {
            delay(6000) // Auto-dismiss after 6 seconds
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it + 100 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it + 100 }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .wrapContentHeight(), // يتمدد مرناً لمنع القص
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // لون كحلي متناسق
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // أيقونة الرسائل الفاخرة
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AccentGold, // ذهبي
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // النص التوضيحي بالكامل دون بتر
                Text(
                    text = "تم الحفظ بنجاح! هل تريد إرسال رسالة نصية بالملخص المالي للموظف؟",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f) // لمنع النص من الخروج والقص
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // أزرار التحكم الجانبية بوضوح
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Color.Gray)
                    }
                    Button(
                        onClick = onSend,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Text("إرسال", color = Color.Black)
                    }
                }
                }
            }
        }
    }
}
