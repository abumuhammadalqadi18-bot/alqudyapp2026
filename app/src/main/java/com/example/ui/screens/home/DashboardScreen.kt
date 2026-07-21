package com.example.ui.screens.home

import androidx.compose.ui.draw.rotate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalCurrencySymbol
import com.example.ui.viewmodels.HomeViewModel
import com.example.util.toCurrencyFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    homeViewModel: HomeViewModel,
    onNavigateToEmployees: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAdvances: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAddEmployee: () -> Unit,
    onNavigateToPayment: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val currency = LocalCurrencySymbol.current

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        homeViewModel.loadDashboardData()
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.logo_pen_scales_1784466975758),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "أهلاً بك، المدير 👋",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "ملخص اليوم: ${SimpleDateFormat("dd MMMM yyyy", Locale("ar")).format(Date())}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExpandableFab(
                onNavigateToAddEmployee = onNavigateToAddEmployee,
                onNavigateToAdvance = onNavigateToAdvances,
                onNavigateToPayment = onNavigateToPayment
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // 4 Cards Grid
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) { it / 3 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "إجمالي العمال",
                            value = "${uiState.activeEmployeeCount}",
                            icon = Icons.Default.People,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "إجمالي الأجور",
                            value = "${uiState.totalPayableToday.toCurrencyFormat()}",
                            currency = currency,
                            icon = Icons.Default.Payments,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "إجمالي السحوبات",
                            value = "${uiState.totalWithdrawalsToday.toCurrencyFormat()}",
                            currency = currency,
                            icon = Icons.Default.MoneyOff,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "إجمالي المدفوعات",
                            value = "${uiState.totalPaymentsThisMonth.toCurrencyFormat()}",
                            currency = currency,
                            icon = Icons.Default.AccountBalanceWallet,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Operations
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 100)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) { it / 3 }
            ) {
                Column {
                    SectionTitle(title = "آخر العمليات")
                    if (uiState.recentWages.isEmpty()) {
                        EmptyStateText("لا توجد عمليات مسجلة حديثاً.")
                    } else {
                        uiState.recentWages.forEach { wage ->
                            TransactionItem(
                                title = "دوام يومية",
                                amount = "${wage.finalAmount.toCurrencyFormat()} $currency",
                                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar")).format(Date(wage.wageDate)),
                                icon = Icons.Default.CheckCircle,
                                iconTint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Withdrawals
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(700, delayMillis = 150)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) { it / 3 }
            ) {
                Column {
                    SectionTitle(title = "آخر الدفعات")
                    if (uiState.recentWithdrawals.isEmpty()) {
                        EmptyStateText("لا توجد دفعات مسجلة حديثاً.")
                    } else {
                        uiState.recentWithdrawals.forEach { withdrawal ->
                            TransactionItem(
                                title = "سلفة أو دفعة",
                                amount = "${withdrawal.amount.toCurrencyFormat()} $currency",
                                date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar")).format(Date(withdrawal.withdrawalDate)),
                                icon = Icons.Default.ArrowDownward,
                                iconTint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Financial Summary
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) { it / 3 }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "الملخص المالي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SummaryRow("إجمالي الأجور المحتسبة", "${uiState.totalPaymentsThisMonth.toCurrencyFormat()} $currency")
                        Spacer(modifier = Modifier.height(12.dp))
                        SummaryRow("إجمالي السحوبات المدفوعة", "${uiState.totalWithdrawalsThisMonth.toCurrencyFormat()} $currency", MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        val net = uiState.totalPaymentsThisMonth - uiState.totalWithdrawalsThisMonth
                        SummaryRow(
                            "الصافي المتبقي", 
                            "${net.toCurrencyFormat()} $currency", 
                            if (net >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            isBold = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(900, delayMillis = 250)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
                ) { it / 3 }
            ) {
                Column {
                    SectionTitle(title = "إجراءات سريعة")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { QuickActionItem("إضافة عامل", Icons.Default.PersonAdd) { onNavigateToAddEmployee() } }
                        item { QuickActionItem("إضافة دوام", Icons.Default.MoreTime) { onNavigateToAttendance() } }
                        item { QuickActionItem("إضافة سلفة", Icons.Default.MoneyOff) { onNavigateToAdvances() } }
                        item { QuickActionItem("إضافة دفعة", Icons.Default.Payments) { onNavigateToPayment() } }
                        item { QuickActionItem("إنشاء تقرير", Icons.Default.Assessment) { onNavigateToReports() } }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    currency: String? = null,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (currency != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun EmptyStateText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun TransactionItem(
    title: String,
    amount: String,
    date: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = iconTint
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String, 
    value: String, 
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}

@Composable
fun QuickActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ExpandableFab(
    onNavigateToAddEmployee: () -> Unit,

    onNavigateToAdvance: () -> Unit,
    onNavigateToPayment: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(initialOffsetY = { it / 2 }),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabMenuItem(text = "إضافة عامل", icon = Icons.Default.PersonAdd, onClick = {
                    expanded = false
                    onNavigateToAddEmployee()
                })
                FabMenuItem(text = "إضافة سلفة", icon = Icons.Default.MoneyOff, onClick = {
                    expanded = false
                    onNavigateToAdvance()
                })
                FabMenuItem(text = "إضافة دفعة", icon = Icons.Default.Payments, onClick = {
                    expanded = false
                    onNavigateToPayment()
                })
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            val rotation by androidx.compose.animation.core.animateFloatAsState(
                targetValue = if (expanded) 45f else 0f
            )
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (expanded) "إغلاق" else "إضافة",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
fun FabMenuItem(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 2.dp,
            modifier = Modifier.clickable { onClick() }
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape
        ) {
            Icon(imageVector = icon, contentDescription = text)
        }
    }
}
