import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

target = """    val uiState: StateFlow<ReportUiState> = combine(
        allEmployeesFlow,
        _selectedEmployeeId,
        _generalFilter,
        transactionsFlow
    ) { employees, selectedEmpId, generalFilter, txs ->"""

replacement = """    private val settingsFlow = combine(
        settingsRepository.getSettingFlow("company_name", ""),
        settingsRepository.getSettingFlow("company_phone", ""),
        settingsRepository.getSettingFlow("company_address", ""),
        settingsRepository.getSettingFlow("company_footer", ""),
        settingsRepository.getSettingFlow("company_logo", ""),
        settingsRepository.getSettingFlow("company_seal", ""),
        settingsRepository.getSettingFlow("company_signature", "")
    ) { name, phone, address, footer, logo, seal, signature ->
        SettingsUiState(
            companyName = name,
            phoneNumbers = phone,
            address = address,
            footerNote = footer,
            logoUri = logo,
            sealUri = seal,
            signatureUri = signature
        )
    }

    val uiState: StateFlow<ReportUiState> = combine(
        allEmployeesFlow,
        _selectedEmployeeId,
        _generalFilter,
        transactionsFlow,
        settingsFlow
    ) { employees, selectedEmpId, generalFilter, txs, settingsState ->"""

if target in content:
    content = content.replace(target, replacement)
    
target2 = """        ReportUiState(
            employees = employees,
            selectedEmployeeId = selectedEmpId,
            employeeTransactions = employeeTransactions.sortedByDescending { it.date },
            employeeSummary = employeeSummary,
            generalFilter = generalFilter,
            generalSummary = generalSummary,
            generalRecords = generalRecords,
            isLoading = false
        )
    }"""
    
replacement2 = """        ReportUiState(
            employees = employees,
            selectedEmployeeId = selectedEmpId,
            employeeTransactions = employeeTransactions.sortedByDescending { it.date },
            employeeSummary = employeeSummary,
            generalFilter = generalFilter,
            generalSummary = generalSummary,
            generalRecords = generalRecords,
            isLoading = false,
            settings = settingsState
        )
    }"""

if target2 in content:
    content = content.replace(target2, replacement2)
    
with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
    f.write(content)
print("Updated State values")
