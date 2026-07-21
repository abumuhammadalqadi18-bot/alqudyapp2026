package com.example.data.repository

import com.example.data.local.dao.SettingDao
import com.example.data.local.entity.SettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository class for managing application settings.
 * مستودع إدارة إعدادات التطبيق مثل (تفعيل القفل، نوع القفل، العملة المستخدمة).
 */
class SettingsRepository(private val settingDao: SettingDao) {

    suspend fun saveSetting(key: String, value: String) {
        settingDao.insertOrUpdate(SettingEntity(key, value))
    }

    suspend fun getSetting(key: String, defaultValue: String = ""): String {
        return settingDao.getSetting(key)?.value ?: defaultValue
    }

    fun getSettingFlow(key: String, defaultValue: String = ""): Flow<String> {
        return settingDao.getSettingFlow(key).map { it?.value ?: defaultValue }
    }

    suspend fun getBooleanSetting(key: String, defaultValue: Boolean = false): Boolean {
        val value = getSetting(key)
        return if (value.isEmpty()) defaultValue else value.toBooleanStrictOrNull() ?: defaultValue
    }

    fun getBooleanSettingFlow(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        return getSettingFlow(key).map { 
            if (it.isEmpty()) defaultValue else it.toBooleanStrictOrNull() ?: defaultValue 
        }
    }

    suspend fun saveBooleanSetting(key: String, value: Boolean) {
        saveSetting(key, value.toString())
    }
}
