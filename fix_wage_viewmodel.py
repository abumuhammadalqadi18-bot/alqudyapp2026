import re

with open("app/src/main/java/com/example/ui/viewmodels/WageViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "val hasAttendanceForSelectedDate: Boolean = false",
    "val hasAttendanceForSelectedDate: Boolean = false,\n    val submittedEmployeeIds: Set<Long> = emptySet()"
)

replacement = """
                val records = wageRepository.getRecordsInRange(startOfDay, endOfDay).firstOrNull() ?: emptyList()
                val submittedIds = records.map { it.employeeId }.toSet()
                _uiState.value = _uiState.value.copy(
                    hasAttendanceForSelectedDate = records.isNotEmpty(),
                    submittedEmployeeIds = submittedIds
                )
"""
content = re.sub(
    r"val records = wageRepository\.getRecordsInRange\(startOfDay, endOfDay\)\.firstOrNull\(\) \?: emptyList\(\)\s*_uiState\.value = _uiState\.value\.copy\(hasAttendanceForSelectedDate = records\.isNotEmpty\(\)\)",
    replacement.strip(),
    content
)

# Also add a method to delete a wage record
delete_method = """
    fun deleteWage(employeeId: Long, date: Long) {
        viewModelScope.launch {
            try {
                val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = date
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
                calendar.set(java.util.Calendar.MINUTE, 59)
                calendar.set(java.util.Calendar.SECOND, 59)
                calendar.set(java.util.Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis
                
                val records = wageRepository.getRecordsInRange(startOfDay, endOfDay).firstOrNull() ?: emptyList()
                val record = records.find { it.employeeId == employeeId }
                if (record != null) {
                    wageRepository.deleteWageRecord(record)
                    checkAttendanceForDate(date)
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
"""
# append delete_method before the last brace
if "fun deleteWage" not in content:
    content = content.rsplit("}", 1)[0] + delete_method + "\n}"

with open("app/src/main/java/com/example/ui/viewmodels/WageViewModel.kt", "w") as f:
    f.write(content)
