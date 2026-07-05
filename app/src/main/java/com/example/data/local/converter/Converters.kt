package com.example.data.local.converter

import androidx.room.TypeConverter
import com.example.domain.model.AdjustmentType
import com.example.domain.model.BackupLocation
import com.example.domain.model.BackupStatus
import com.example.domain.model.DayType
import com.example.domain.model.EmployeeStatus
import com.example.domain.model.WithdrawalType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromEmployeeStatus(value: EmployeeStatus): String = value.name

    @TypeConverter
    fun toEmployeeStatus(value: String): EmployeeStatus = enumValueOf(value)

    @TypeConverter
    fun fromDayType(value: DayType): String = value.name

    @TypeConverter
    fun toDayType(value: String): DayType = enumValueOf(value)

    @TypeConverter
    fun fromWithdrawalType(value: WithdrawalType): String = value.name

    @TypeConverter
    fun toWithdrawalType(value: String): WithdrawalType = enumValueOf(value)

    @TypeConverter
    fun fromAdjustmentType(value: AdjustmentType): String = value.name

    @TypeConverter
    fun toAdjustmentType(value: String): AdjustmentType = enumValueOf(value)

    @TypeConverter
    fun fromBackupLocation(value: BackupLocation): String = value.name

    @TypeConverter
    fun toBackupLocation(value: String): BackupLocation = enumValueOf(value)

    @TypeConverter
    fun fromBackupStatus(value: BackupStatus): String = value.name

    @TypeConverter
    fun toBackupStatus(value: String): BackupStatus = enumValueOf(value)
}
