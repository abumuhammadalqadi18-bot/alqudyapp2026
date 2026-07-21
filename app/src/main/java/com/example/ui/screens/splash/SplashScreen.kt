package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToLock: () -> Unit
) {
    val uiState by settingsViewModel.uiState.collectAsState()
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
            delay(2500)
            if (uiState.isPinLockEnabled || uiState.isBiometricEnabled) {
                onNavigateToLock()
            } else {
                onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAFA)), // Premium light cyan-white base
        contentAlignment = Alignment.Center
    ) {
        SplashBackground()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp)
                .alpha(alpha.value)
        ) {
            Spacer(modifier = Modifier.weight(0.35f))
            
            GlassSphere()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "القاضي",
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF094B48), // Dark emerald green
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gold Diamond Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.width(180.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFD4AF37).copy(alpha = 0.5f)))
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .size(8.dp)
                        .rotate(45f)
                        .background(Color(0xFFD4AF37))
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFD4AF37).copy(alpha = 0.5f)))
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Text(
                text = "لإدارة الأجور والأعمال\nالعدل في كل حساب",
                fontSize = 22.sp,
                lineHeight = 36.sp,
                color = Color(0xFF6B7B83), // Medium gray-cyan
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.weight(0.65f))
            
            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD1E6E4)))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF117F7A))) // Active Indicator
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD1E6E4)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD1E6E4)))
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD1E6E4)))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Version 1.0",
                fontSize = 14.sp,
                color = Color(0xFF7A848A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SplashBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top right wave
        val topWave = Path().apply {
            moveTo(width * 0.1f, 0f)
            cubicTo(
                width * 0.6f, height * 0.1f,
                width * 0.5f, height * 0.3f,
                width, height * 0.45f
            )
            lineTo(width, 0f)
            close()
        }
        drawPath(
            path = topWave,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE2F3F2),
                    Color(0xFFF4FAFA).copy(alpha = 0.2f)
                ),
                start = Offset(width * 0.5f, 0f),
                end = Offset(width, height * 0.4f)
            )
        )

        // Bottom left sweeping light background wave
        val bottomWaveLight = Path().apply {
            moveTo(0f, height * 0.4f)
            cubicTo(
                width * 0.6f, height * 0.55f,
                width * 0.2f, height * 0.85f,
                width * 1.2f, height * 0.95f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = bottomWaveLight,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE2F1F0).copy(alpha = 0.8f),
                    Color(0xFFCDE8E6).copy(alpha = 0.4f)
                ),
                start = Offset(0f, height * 0.4f),
                end = Offset(width, height * 0.9f)
            )
        )

        // Bottom left medium teal wave
        val bottomWaveMid = Path().apply {
            moveTo(0f, height * 0.55f)
            cubicTo(
                width * 0.5f, height * 0.65f,
                width * 0.2f, height * 0.9f,
                width * 0.9f, height
            )
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = bottomWaveMid,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF67BDB8).copy(alpha = 0.5f),
                    Color(0xFF2A8B86).copy(alpha = 0.7f)
                ),
                start = Offset(0f, height * 0.6f),
                end = Offset(width * 0.7f, height)
            )
        )

        // Bottom left dark teal wave
        val bottomWaveDark = Path().apply {
            moveTo(0f, height * 0.65f)
            cubicTo(
                width * 0.4f, height * 0.7f,
                width * 0.3f, height * 0.95f,
                width * 0.8f, height
            )
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = bottomWaveDark,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF147D7A).copy(alpha = 0.9f),
                    Color(0xFF094B48)
                ),
                start = Offset(0f, height * 0.7f),
                end = Offset(width * 0.5f, height)
            )
        )

        // Glowing spots
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = 5f,
            center = Offset(width * 0.1f, height * 0.78f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = 15f,
            center = Offset(width * 0.1f, height * 0.78f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = 3f,
            center = Offset(width * 0.25f, height * 0.88f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = 10f,
            center = Offset(width * 0.25f, height * 0.88f)
        )
        
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = 4f,
            center = Offset(width * 0.05f, height * 0.83f)
        )
        
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = 2.5f,
            center = Offset(width * 0.15f, height * 0.93f)
        )

        // Random dust dots
        val dustPositions = listOf(
            Offset(width * 0.8f, height * 0.1f),
            Offset(width * 0.85f, height * 0.15f),
            Offset(width * 0.9f, height * 0.08f),
            Offset(width * 0.95f, height * 0.12f),
            Offset(width * 0.75f, height * 0.2f),
            Offset(width * 0.82f, height * 0.25f),
            Offset(width * 0.85f, height * 0.9f),
            Offset(width * 0.9f, height * 0.85f),
            Offset(width * 0.95f, height * 0.92f),
            Offset(width * 0.88f, height * 0.95f),
        )
        dustPositions.forEach { pos ->
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 1.5f,
                center = pos
            )
        }
    }
}

@Composable
fun GlassSphere(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Drop shadow (cyan glow)
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(y = 20.dp)
                .blur(40.dp)
                .background(Color(0xFF38E0D1).copy(alpha = 0.5f), CircleShape)
        )

        // Glass Base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f), // Center bright
                            Color.White.copy(alpha = 0.1f),
                            Color(0xFF9DF0E8).copy(alpha = 0.5f) // Outer cyan rim
                        ),
                        center = Offset(140f, 140f),
                        radius = 400f
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.Transparent,
                            Color(0xFF75E6DB).copy(alpha = 0.8f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(800f, 800f)
                    ),
                    shape = CircleShape
                )
        )

        // Inner Cyan Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color(0xFF67E2D9).copy(alpha = 0.3f)),
                        center = Offset(200f, 200f),
                        radius = 350f
                    )
                )
        )

        // Logo Image
        Image(
            painter = painterResource(id = com.example.R.drawable.logo_pen_scales_1784466975758),
            contentDescription = "Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF042422),
                    spotColor = Color(0xFF042422).copy(alpha = 0.7f)
                )
        )

        // Specular highlights overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Top left strong highlight arc
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.95f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(canvasWidth * 0.7f, canvasHeight * 0.7f)
                ),
                startAngle = 175f,
                sweepAngle = 105f,
                useCenter = false,
                style = Stroke(width = 24f, cap = StrokeCap.Round),
                topLeft = Offset(18f, 18f),
                size = Size(canvasWidth - 36f, canvasHeight - 36f)
            )

            // Bottom right subtle cyan highlight arc
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color(0xFF5CE0D6).copy(alpha = 0.85f)),
                    start = Offset(canvasWidth * 0.3f, canvasHeight * 0.3f),
                    end = Offset(canvasWidth, canvasHeight)
                ),
                startAngle = -5f,
                sweepAngle = 85f,
                useCenter = false,
                style = Stroke(width = 16f, cap = StrokeCap.Round),
                topLeft = Offset(26f, 26f),
                size = Size(canvasWidth - 52f, canvasHeight - 52f)
            )
        }
    }
}
