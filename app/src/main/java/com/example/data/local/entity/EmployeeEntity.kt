package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.EmployeeStatus

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    @ColumnInfo(name = "national_id")
    val nationalId: String?,
    @ColumnInfo(name = "job_title")
    val jobTitle: String,
    val department: String?,
    @ColumnInfo(name = "current_daily_wage")
    val currentDailyWage: Double,
    @ColumnInfo(name = "hire_date")
    val hireDate: Long,
    val status: EmployeeStatus = EmployeeStatus.ACTIVE,
    @ColumnInfo(name = "photo_path")
    val photoPath: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
