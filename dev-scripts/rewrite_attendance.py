import re

with open("app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt", "r") as f:
    content = f.read()

# We will just replace the entire content since we need to rewrite it completely to be safe.
# Or we can replace the bottomBar and the Card.

# First, remove the global approve button from bottom bar
bottom_bar_replacement = """        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color(0xFF0F1B2B)) // Always dark navy regardless of theme
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي الأجور المستحقة اليوم",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${totalWage.toCurrencyFormat()} $currency",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AccentGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "تم اعتماد: ${submittedEmployeeIds.size} / $totalCount",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }"""

# Next, we need to modify the card so it has an approve button
# The card needs context, scope, settingsState, reportViewModel to send SMS?
# Yes, or we do the action in the LazyColumn items.
# Let's write the full file.
