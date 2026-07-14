import os

files = [
    'app/src/test/java/com/example/qadi/BackupRestoreTest.kt',
    'app/src/test/java/com/example/qadi/NavigationAndRtlUiTest.kt',
    'app/src/test/java/com/example/qadi/WageAndBalanceTest.kt'
]

for f in files:
    with open(f, 'r') as file:
        content = file.read()
    
    # Fix EmployeeEntity
    content = content.replace(
        'createdAt = System.currentTimeMillis()',
        'phone = "", nationalId = null, department = null, hireDate = System.currentTimeMillis(), photoPath = null, notes = null, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()'
    )
    
    # Fix WageRecordEntity
    content = content.replace(
        'calculatedAmount = 10000.0, finalAmount = 10000.0, createdAt = 1000L, updatedAt = 1000L',
        'hoursWorked = null, calculatedAmount = 10000.0, finalAmount = 10000.0, notes = null, createdAt = 1000L, updatedAt = 1000L'
    )
    content = content.replace(
        'calculatedAmount = 10000.0, finalAmount = 10000.0, createdAt = 2000L, updatedAt = 2000L',
        'hoursWorked = null, calculatedAmount = 10000.0, finalAmount = 10000.0, notes = null, createdAt = 2000L, updatedAt = 2000L'
    )
    content = content.replace(
        'calculatedAmount = 15000.0,\n                finalAmount = 15000.0,\n                createdAt = 10000L,\n                updatedAt = 10000L',
        'hoursWorked = null,\n                calculatedAmount = 15000.0,\n                finalAmount = 15000.0,\n                notes = null,\n                createdAt = 10000L,\n                updatedAt = 10000L'
    )

    # Fix WithdrawalEntity
    content = content.replace(
        'amount = 2000.0, withdrawalDate = 4000L, createdAt = 4000L, updatedAt = 4000L',
        'amount = 2000.0, withdrawalDate = 4000L, withdrawalType = com.example.domain.model.WithdrawalType.ADVANCE, description = null, createdAt = 4000L, updatedAt = 4000L'
    )
    content = content.replace(
        'amount = 3000.0,\n                withdrawalDate = 20000L,\n                createdAt = 20000L,\n                updatedAt = 20000L',
        'amount = 3000.0,\n                withdrawalDate = 20000L,\n                withdrawalType = com.example.domain.model.WithdrawalType.ADVANCE,\n                description = null,\n                createdAt = 20000L,\n                updatedAt = 20000L'
    )
    
    # Fix AdjustmentEntity
    content = content.replace(
        'type = AdjustmentType.BONUS, createdAt = 3000L, updatedAt = 3000L',
        'type = AdjustmentType.BONUS, reason = null, createdAt = 3000L'
    )
    content = content.replace(
        'type = AdjustmentType.DEDUCTION, createdAt = 5000L, updatedAt = 5000L',
        'type = AdjustmentType.DEDUCTION, reason = null, createdAt = 5000L'
    )

    with open(f, 'w') as file:
        file.write(content)

