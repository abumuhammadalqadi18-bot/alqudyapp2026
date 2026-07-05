package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wage_rate_history",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employee_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["employee_id"])]
)
data class WageRateHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Long,
    @ColumnInfo(name = "old_daily_wage")
    val oldDailyWage: Double,
    @ColumnInfo(name = "new_daily_wage")
    val newDailyWage: Double,
    @ColumnInfo(name = "effective_from_date")
    val effectiveFromDate: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
