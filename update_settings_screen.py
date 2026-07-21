import sys

with open('app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Add imports
imports = """import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import coil.compose.AsyncImage
import com.example.ui.viewmodels.SettingsUiState"""

if "import coil.compose.AsyncImage" not in content:
    content = content.replace("import java.util.Locale\n", "import java.util.Locale\n" + imports + "\n")

# 2. Inject Company Profile section
target = """            // 2. SMS Notifications
            item {"""

replacement = """            // 1.5 Company Profile
            item {
                SettingsSectionTitle("إعدادات هوية المؤسسة والوثائق")
                SettingsCard {
                    var profileExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { profileExpanded = !profileExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("هوية المؤسسة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("اسم الشركة، اللوجو، الأرقام، الختم والتوقيع", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = if (profileExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (profileExpanded) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        CompanyProfileForm(settingsViewModel = settingsViewModel, uiState = uiState)
                    }
                }
            }

            // 2. SMS Notifications
            item {"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Failed to find injection point")

# 3. Append CompanyProfileForm function
append_code = """

@Composable
fun CompanyProfileForm(settingsViewModel: SettingsViewModel, uiState: SettingsUiState) {
    var companyName by remember { mutableStateOf(uiState.companyName) }
    var phoneNumbers by remember { mutableStateOf(uiState.phoneNumbers) }
    var address by remember { mutableStateOf(uiState.address) }
    var services by remember { mutableStateOf(uiState.services) }
    var footerNote by remember { mutableStateOf(uiState.footerNote) }
    
    var logoUri by remember { mutableStateOf<android.net.Uri?>(uiState.logoUri?.let { android.net.Uri.parse(it) }) }
    var sealUri by remember { mutableStateOf<android.net.Uri?>(uiState.sealUri?.let { android.net.Uri.parse(it) }) }
    var signatureUri by remember { mutableStateOf<android.net.Uri?>(uiState.signatureUri?.let { android.net.Uri.parse(it) }) }

    val context = LocalContext.current

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            logoUri = it 
        }
    }
    
    val sealPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            sealUri = it 
        }
    }
    
    val signaturePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {}
            signatureUri = it 
        }
    }

    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("اسم المؤسسة") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = phoneNumbers,
            onValueChange = { phoneNumbers = it },
            label = { Text("أرقام التواصل") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("العنوان") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = services,
            onValueChange = { services = it },
            label = { Text("الخدمات أو المنتجات") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = footerNote,
            onValueChange = { footerNote = it },
            label = { Text("ملاحظة ختامية للتقارير") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("الوثائق الرسمية", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("شعار المؤسسة", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = logoUri,
                    contentDescription = "شعار",
                    modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.LightGray)
                )
                Button(onClick = { logoPicker.launch("image/*") }) { Text("اختيار") }
            }
        }
        
        // Seal
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("الختم الرسمي", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = sealUri,
                    contentDescription = "ختم",
                    modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color.LightGray)
                )
                Button(onClick = { sealPicker.launch("image/*") }) { Text("اختيار") }
            }
        }
        
        // Signature
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("توقيع المدير", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = signatureUri,
                    contentDescription = "توقيع",
                    modifier = Modifier.size(60.dp, 30.dp).background(Color.LightGray)
                )
                Button(onClick = { signaturePicker.launch("image/*") }) { Text("اختيار") }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                settingsViewModel.updateCompanyProfile(
                    name = companyName,
                    phone = phoneNumbers,
                    address = address,
                    services = services,
                    footer = footerNote,
                    logoUri = logoUri?.toString(),
                    sealUri = sealUri?.toString(),
                    signatureUri = signatureUri?.toString()
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy)
        ) {
            Text("حفظ بيانات المؤسسة", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
"""

content += append_code

with open('app/src/main/java/com/example/ui/screens/settings/SettingsScreen.kt', 'w') as f:
    f.write(content)

print("SettingsScreen updated successfully!")
