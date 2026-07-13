import re

with open("app/src/main/java/com/example/ui/viewmodels/WageViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "hoursWorked: Double? = null,\n        notes: String? = null",
    "hoursWorked: Double? = null,\n        notes: String? = null,\n        finalAmountOverride: Double? = null"
)

content = content.replace(
    "val finalAmount = calculatedAmount",
    "val finalAmount = finalAmountOverride ?: calculatedAmount"
)

with open("app/src/main/java/com/example/ui/viewmodels/WageViewModel.kt", "w") as f:
    f.write(content)
