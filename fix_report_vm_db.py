import sys

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'r') as f:
    content = f.read()

target_imports = "import com.example.ui.viewmodels.SettingsUiState"
replacement_imports = """import com.example.data.repository.CompanyProfileRepository
import com.example.ui.viewmodels.SettingsUiState"""

if "CompanyProfileRepository" not in content:
    content = content.replace("import com.example.ui.viewmodels.SettingsUiState", replacement_imports)

target_constructor = """class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {"""

replacement_constructor = """class ReportViewModel(
    private val employeeRepository: EmployeeRepository,
    private val wageRepository: WageRepository,
    private val financeRepository: FinanceRepository,
    private val settingsRepository: SettingsRepository,
    private val companyProfileRepository: CompanyProfileRepository
) : ViewModel() {"""

if target_constructor in content:
    content = content.replace(target_constructor, replacement_constructor)

target_settings_flow = """    private val settingsFlow = combine(
        combine(
            settingsRepository.getSettingFlow("company_name", ""),
            settingsRepository.getSettingFlow("company_phone", ""),
            settingsRepository.getSettingFlow("company_address", "")
        ) { name, phone, address -> Triple(name, phone, address) },
        combine(
            settingsRepository.getSettingFlow("company_footer", ""),
            settingsRepository.getSettingFlow("company_logo", ""),
            settingsRepository.getSettingFlow("company_seal", ""),
            settingsRepository.getSettingFlow("company_signature", "")
        ) { footer, logo, seal, signature -> listOf(footer, logo, seal, signature) }
    ) { group1, group2 ->
        SettingsUiState(
            companyName = group1.first,
            phoneNumbers = group1.second,
            address = group1.third,
            footerNote = group2[0],
            logoUri = group2[1],
            sealUri = group2[2],
            signatureUri = group2[3]
        )
    }"""

replacement_settings_flow = """    private val settingsFlow = companyProfileRepository.getProfileFlow().map { profile ->
        if (profile != null) {
            SettingsUiState(
                companyName = profile.companyName,
                phoneNumbers = profile.phoneNumbers,
                address = profile.address,
                footerNote = profile.footerNote,
                logoUri = profile.logoUri,
                sealUri = profile.sealUri,
                signatureUri = profile.signatureUri
            )
        } else {
            SettingsUiState()
        }
    }"""

if target_settings_flow in content:
    content = content.replace(target_settings_flow, replacement_settings_flow)

with open('app/src/main/java/com/example/ui/viewmodels/ReportViewModel.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'r') as f:
    provider_content = f.read()

target_provider = """            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository,
                    appContainer.settingsRepository
                ) as T
            }"""

replacement_provider = """            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(
                    appContainer.employeeRepository,
                    appContainer.wageRepository,
                    appContainer.financeRepository,
                    appContainer.settingsRepository,
                    appContainer.companyProfileRepository
                ) as T
            }"""

if target_provider in provider_content:
    provider_content = provider_content.replace(target_provider, replacement_provider)
    with open('app/src/main/java/com/example/ui/viewmodels/AppViewModelProvider.kt', 'w') as f:
        f.write(provider_content)

print("Updated ReportViewModel")
