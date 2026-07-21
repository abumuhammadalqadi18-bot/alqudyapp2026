package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import com.example.ui.theme.PrimaryTeal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.home.DashboardScreen
import com.example.ui.screens.lock.LockScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.theme.AccentGold
import com.example.ui.viewmodels.AppViewModelProvider

@Composable
fun AppNavigation(
    viewModelProvider: AppViewModelProvider,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute in BottomNavScreens.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant, // Using surfaceVariant for bottom bar (#F2F2F2 like)
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    BottomNavScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { 
                                Text(
                                    text = screen.title, 
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryTeal, // Dark navy for selected icon
                                selectedTextColor = PrimaryTeal,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                indicatorColor = AccentGold.copy(alpha = 0.3f) // Pale gold indicator
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(if (showBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues()),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                SplashScreen(
                    settingsViewModel = viewModel(factory = viewModelProvider),
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLock = {
                        navController.navigate(Screen.Lock.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.Lock.route,
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                LockScreen(
                    settingsViewModel = viewModel(factory = viewModelProvider),
                    onUnlockSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Lock.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Home.route) {
                DashboardScreen(
                    homeViewModel = viewModel(factory = viewModelProvider),
                    onNavigateToEmployees = { navController.navigate(Screen.Employees.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                    onNavigateToAdvances = { navController.navigate(Screen.Withdraw.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToAddEmployee = { navController.navigate(Screen.AddEditEmployee.route) },
                    onNavigateToPayment = { navController.navigate(Screen.Payment.route) }
                )
            }

            composable(Screen.Employees.route) {
                com.example.ui.screens.employee.EmployeesScreen(
                    employeeViewModel = viewModel(factory = viewModelProvider),
                    onNavigateToAddEdit = { employeeId ->
                        val route = if (employeeId != null) "${Screen.AddEditEmployee.route}/$employeeId" else Screen.AddEditEmployee.route
                        navController.navigate(route)
                    },
                    onNavigateToReport = {
                        navController.navigate(Screen.Reports.route)
                    }
                )
            }

            composable(
                route = "${Screen.AddEditEmployee.route}?id={id}",
                arguments = listOf(androidx.navigation.navArgument("id") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("id")
                val employeeId = idStr?.toLongOrNull()
                com.example.ui.screens.employee.AddEditEmployeeScreen(
                    employeeId = employeeId,
                    employeeViewModel = viewModel(factory = viewModelProvider),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Screen.AddEditEmployee.route}/{id}",
                arguments = listOf(androidx.navigation.navArgument("id") { 
                    type = androidx.navigation.NavType.StringType 
                })
            ) { backStackEntry ->
                val idStr = backStackEntry.arguments?.getString("id")
                val employeeId = idStr?.toLongOrNull()
                com.example.ui.screens.employee.AddEditEmployeeScreen(
                    employeeId = employeeId,
                    employeeViewModel = viewModel(factory = viewModelProvider),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Attendance.route) {
                com.example.ui.screens.attendance.AttendanceScreen(
                    wageViewModel = viewModel(factory = viewModelProvider),
                    reportViewModel = viewModel(factory = viewModelProvider),
                    settingsViewModel = viewModel(factory = viewModelProvider),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Payment.route) {
                com.example.ui.screens.payment.PaymentScreen(
                    financeViewModel = viewModel(factory = viewModelProvider),
                    employeeViewModel = viewModel(factory = viewModelProvider),
                    reportViewModel = viewModel(factory = viewModelProvider),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Withdraw.route) {
                com.example.ui.screens.withdraw.WithdrawScreen(
                    financeViewModel = viewModel(factory = viewModelProvider),
                    employeeViewModel = viewModel(factory = viewModelProvider),
                    settingsViewModel = viewModel(factory = viewModelProvider),
                    reportViewModel = viewModel(factory = viewModelProvider),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Reports.route) {
                com.example.ui.screens.reports.ReportScreen(
                    reportViewModel = viewModel(factory = viewModelProvider)
                )
            }

            composable(Screen.Settings.route) {
                com.example.ui.screens.settings.SettingsScreen(
                    settingsViewModel = viewModel(factory = viewModelProvider)
                )
            }
        }
    }
}
