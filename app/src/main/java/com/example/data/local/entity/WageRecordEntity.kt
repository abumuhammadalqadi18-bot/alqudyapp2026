package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.DayType

@Entity(
    tableName = "wage_records",
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
data class WageRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Long,
    @ColumnInfo(name = "wage_date")
    val wageDate: Long,
    @ColumnInfo(name = "day_type")
    val dayType: DayType,
    @ColumnInfo(name = "hours_worked")
    val hoursWorked: Double?,
    @ColumnInfo(name = "calculated_amount")
    val calculatedAmount: Double,
    @ColumnInfo(name = "final_amount")
    val finalAmount: Double,
    val notes: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
