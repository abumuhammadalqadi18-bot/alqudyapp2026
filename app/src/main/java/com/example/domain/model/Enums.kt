package com.example.domain.model

enum class EmployeeStatus {
    ACTIVE,
    INACTIVE
}

enum class DayType {
    FULL_DAY,
    HALF_DAY,
    HOURS,
    ABSENT,
    LATE,
    CUSTOM
}

enum class WithdrawalType {
    CASH,
    DEFERRED
}

enum class AdjustmentType {
    BONUS,
    DEDUCTION
}

enum class BackupLocation {
    LOCAL,
    DRIVE
}

enum class BackupStatus {
    SUCCESS,
    FAILED
}
