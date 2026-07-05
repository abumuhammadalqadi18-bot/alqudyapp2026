package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen definitions for the application navigation.
 * تعريف مسارات واجهات التطبيق وشريط التنقل.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Outlined.Home) // Icon not used
    object Lock : Screen("lock", "Lock", Icons.Outlined.Home) // Icon not used
    object Home : Screen("home", "الرئيسية", Icons.Outlined.Home)
    object Employees : Screen("employees", "الموظفين", Icons.Outlined.Person)
    object AddEditEmployee : Screen("add_edit_employee", "إضافة/تعديل موظف", Icons.Outlined.Person)
    object Attendance : Screen("attendance", "الحضور والغياب", Icons.Outlined.DateRange)
    object Withdraw : Screen("withdraw", "تسجيل سحب", Icons.Outlined.Home)
    object Reports : Screen("reports", "التقارير", Icons.AutoMirrored.Outlined.List)
    object Settings : Screen("settings", "الإعدادات", Icons.Outlined.Settings)
}

val BottomNavScreens = listOf(
    Screen.Home,
    Screen.Employees,
    Screen.Reports,
    Screen.Settings
)
