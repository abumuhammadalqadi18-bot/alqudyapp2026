package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AdjustmentType

@Entity(
    tableName = "adjustments",
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
data class AdjustmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "employee_id")
    val employeeId: Long,
    @ColumnInfo(name = "adjustment_date")
    val adjustmentDate: Long,
    val type: AdjustmentType,
    val amount: Double,
    val reason: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
