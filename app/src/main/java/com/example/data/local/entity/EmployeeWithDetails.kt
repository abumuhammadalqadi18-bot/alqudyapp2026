package com.example.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class EmployeeWithDetails(
    @Embedded val employee: EmployeeEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "employee_id"
    )
    val wageRecords: List<WageRecordEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "employee_id"
    )
    val withdrawals: List<WithdrawalEntity>,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "employee_id"
    )
    val adjustments: List<AdjustmentEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "employee_id"
    )
    val wageRateHistory: List<WageRateHistoryEntity>
)
