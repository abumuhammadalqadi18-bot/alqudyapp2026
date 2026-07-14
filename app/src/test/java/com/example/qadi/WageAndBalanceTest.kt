package com.example.qadi

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.AlQadiApplication
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.WageRecordEntity
import com.example.data.local.entity.WithdrawalEntity
import com.example.data.local.entity.AdjustmentEntity
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.WageRepository
import com.example.domain.model.DayType
import com.example.domain.model.AdjustmentType
import com.example.ui.viewmodels.ReportViewModel
import com.example.ui.viewmodels.WageViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.lang.reflect.Method
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WageAndBalanceTest {

    private lateinit var employeeRepo: EmployeeRepository
    private lateinit var wageRepo: WageRepository
    private lateinit var financeRepo: FinanceRepository
    private lateinit var wageViewModel: WageViewModel
    private lateinit var reportViewModel: ReportViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<AlQadiApplication>()
        val container = context.container
        
        employeeRepo = container.employeeRepository
        wageRepo = container.wageRepository
        financeRepo = container.financeRepository
        
        wageViewModel = WageViewModel(wageRepo, employeeRepo)
        reportViewModel = ReportViewModel(employeeRepo, wageRepo, financeRepo)
    }

    @Test
    fun testWageCalculationsBasedOnDayType() {
        val method: Method = WageViewModel::class.java.getDeclaredMethod(
            "calculateWageAmount",
            Double::class.java,
            DayType::class.java,
            Double::class.javaObjectType
        )
        method.isAccessible = true

        val baseWage = 10000.0

        val fullDayWage = method.invoke(wageViewModel, baseWage, DayType.FULL_DAY, null) as Double
        assertEquals(10000.0, fullDayWage, 0.01)

        val halfDayWage = method.invoke(wageViewModel, baseWage, DayType.HALF_DAY, null) as Double
        assertEquals(5000.0, halfDayWage, 0.01)

        val hourlyWage = method.invoke(wageViewModel, baseWage, DayType.HOURS, 4.0) as Double
        assertEquals(5000.0, hourlyWage, 0.01)

        val absentWage = method.invoke(wageViewModel, baseWage, DayType.ABSENT, null) as Double
        assertEquals(0.0, absentWage, 0.01)
    }

    @Test
    fun testTotalNetPayableCalculationAndConstraints() = runBlocking {
        // إنشاء موظف بأجر يومي معروف
        val empId = employeeRepo.insertEmployee(
            EmployeeEntity(
                name = "Test Employee", jobTitle = "Tester", currentDailyWage = 10000.0, phone = "", nationalId = null, department = null, hireDate = System.currentTimeMillis(), photoPath = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
        )

        // يومين كاملين
        wageRepo.insertWageRecord(
            WageRecordEntity(
                employeeId = empId, wageDate = 1000L, dayType = DayType.FULL_DAY, hoursWorked = null, calculatedAmount = 10000.0, finalAmount = 10000.0, notes = null, createdAt = 1000L, updatedAt = 1000L
            )
        )
        wageRepo.insertWageRecord(
            WageRecordEntity(
                employeeId = empId, wageDate = 2000L, dayType = DayType.FULL_DAY, hoursWorked = null, calculatedAmount = 10000.0, finalAmount = 10000.0, notes = null, createdAt = 2000L, updatedAt = 2000L
            )
        )

        // مكافأة
        financeRepo.insertAdjustment(
            AdjustmentEntity(
                employeeId = empId, amount = 5000.0, adjustmentDate = 3000L, type = AdjustmentType.BONUS, reason = null, createdAt = 3000L
            )
        )

        // سحب
        financeRepo.insertWithdrawal(
            WithdrawalEntity(
                employeeId = empId, amount = 2000.0, withdrawalDate = 4000L, withdrawalType = com.example.domain.model.WithdrawalType.CASH, description = null, createdAt = 4000L, updatedAt = 4000L
            )
        )

        // خصم
        financeRepo.insertAdjustment(
            AdjustmentEntity(
                employeeId = empId, amount = 1000.0, adjustmentDate = 5000L, type = AdjustmentType.DEDUCTION, reason = null, createdAt = 5000L
            )
        )

        ShadowLooper.idleMainLooper()

        val netPayable = reportViewModel.getNetPayableForEmployee(empId)
        assertEquals(22000.0, netPayable, 0.01)
    }

    @Test
    fun testDayTypeCustom() = runBlocking {
        val empId = employeeRepo.insertEmployee(
            EmployeeEntity(
                name = "Test Employee", jobTitle = "Tester", currentDailyWage = 10000.0, phone = "", nationalId = null, department = null, hireDate = System.currentTimeMillis(), photoPath = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
        )

        wageViewModel.recordWage(
            employeeId = empId,
            date = 1000L,
            dayType = DayType.CUSTOM,
            finalAmountOverride = 7500.0
        )
        
        ShadowLooper.idleMainLooper()

        val record = wageRepo.getRecordsInRange(0L, 2000L).first().find { it.employeeId == empId }
        requireNotNull(record)
        assertEquals(7500.0, record.finalAmount, 0.01)
        assertEquals(0.0, record.calculatedAmount, 0.01)
    }

    @Test
    fun testFinalAmountOverride() = runBlocking {
        val empId = employeeRepo.insertEmployee(
            EmployeeEntity(
                name = "Test Employee", jobTitle = "Tester", currentDailyWage = 10000.0, phone = "", nationalId = null, department = null, hireDate = System.currentTimeMillis(), photoPath = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
        )

        wageViewModel.recordWage(
            employeeId = empId,
            date = 1000L,
            dayType = DayType.FULL_DAY,
            finalAmountOverride = 12000.0
        )
        
        ShadowLooper.idleMainLooper()

        val record = wageRepo.getRecordsInRange(0L, 2000L).first().find { it.employeeId == empId }
        requireNotNull(record)
        assertEquals(10000.0, record.calculatedAmount, 0.01)
        assertEquals(12000.0, record.finalAmount, 0.01)
    }
}
