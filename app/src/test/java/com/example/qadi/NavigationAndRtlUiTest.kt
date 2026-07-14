package com.example.qadi

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.AlQadiApplication
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.WageRecordEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.domain.model.DayType
import com.example.ui.theme.AlQadiTheme
import com.example.ui.viewmodels.ReportViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class NavigationAndRtlUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "ar")
    fun testRtlSupport() {
        var currentLayoutDirection: LayoutDirection? = null
        composeTestRule.setContent {
            AlQadiTheme {
                currentLayoutDirection = LocalLayoutDirection.current
            }
        }
        assertEquals(LayoutDirection.Rtl, currentLayoutDirection)
    }

    @Test
    fun testEndToEndFlow_AddEmployee_AddWage_AddWithdrawal() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<AlQadiApplication>()
        val container = context.container
        
        val employeeRepo = container.employeeRepository
        val wageRepo = container.wageRepository
        val financeRepo = container.financeRepository
        
        val reportViewModel = ReportViewModel(employeeRepo, wageRepo, financeRepo)

        // 1. Add employee
        val empId = employeeRepo.insertEmployee(
            EmployeeEntity(
                name = "Integration Test Employee", jobTitle = "Developer", currentDailyWage = 15000.0, phone = "", nationalId = null, department = null, hireDate = System.currentTimeMillis(), photoPath = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
        )

        // 2. Add wage
        wageRepo.insertWageRecord(
            WageRecordEntity(
                employeeId = empId, wageDate = 10000L, dayType = DayType.FULL_DAY, hoursWorked = null, calculatedAmount = 15000.0, finalAmount = 15000.0, notes = null, createdAt = 10000L, updatedAt = 10000L
            )
        )

        // 3. Add withdrawal
        financeRepo.insertWithdrawal(
            WithdrawalEntity(
                employeeId = empId, amount = 3000.0, withdrawalDate = 20000L, withdrawalType = com.example.domain.model.WithdrawalType.CASH, description = null, createdAt = 20000L, updatedAt = 20000L
            )
        )

        ShadowLooper.idleMainLooper()

        // 4. Verify in ReportViewModel
        reportViewModel.selectEmployee(empId)
        
        ShadowLooper.idleMainLooper()

        val uiState = reportViewModel.uiState.first { !it.isLoading && it.employeeSummary != null }
        val employeeSummary = uiState.employeeSummary

        assertNotNull(employeeSummary)
        assertEquals("Integration Test Employee", employeeSummary?.employeeName)
        assertEquals(15000.0, employeeSummary?.totalEarned)
        assertEquals(3000.0, employeeSummary?.totalWithdrawn)
        assertEquals(12000.0, employeeSummary?.netPayable)
    }
}
