package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.BackupRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.FinanceRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.CompanyProfileRepository
import com.example.data.repository.WageRepository
import com.example.util.KeystoreHelper

/**
 * Manual Dependency Injection container at the application level.
 * This container instantiates and provides dependencies across the app.
 */
class AppContainer(private val applicationContext: Context) {

    private val dbPassphrase = KeystoreHelper.getOrCreatePassphrase(applicationContext)

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(applicationContext, dbPassphrase)
    }

    // DAOs
    val employeeDao by lazy { database.employeeDao() }
    val wageRateHistoryDao by lazy { database.wageRateHistoryDao() }
    val wageRecordDao by lazy { database.wageRecordDao() }
    val withdrawalDao by lazy { database.withdrawalDao() }
    val adjustmentDao by lazy { database.adjustmentDao() }
    val backupHistoryDao by lazy { database.backupHistoryDao() }
    val settingDao by lazy { database.settingDao() }

    // Repositories
    val employeeRepository by lazy { EmployeeRepository(employeeDao) }
    val wageRepository by lazy { WageRepository(wageRecordDao, wageRateHistoryDao) }
    val financeRepository by lazy { FinanceRepository(withdrawalDao, adjustmentDao) }
    val backupRepository by lazy { BackupRepository(backupHistoryDao) }
    val settingsRepository by lazy { SettingsRepository(settingDao) }
    val companyProfileRepository by lazy { CompanyProfileRepository(database.companyProfileDao()) }
}
