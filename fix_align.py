import sys

# 1. Modify AttendanceScreen.kt to align SmsBanner to BottomCenter
with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'r') as f:
    content = f.read()

target = """        com.example.ui.components.SmsBanner(
            isVisible = showSmsBanner,
            onSend = {
                showSmsBanner = false
                // Logic for mass SMS has been replaced with per-employee logic as per requirements.
                // It is better to rely on auto SMS for per-employee. If they click this banner, we could send for all approved today.
            },
            onDismiss = { showSmsBanner = false }
        )"""

replacement = """        com.example.ui.components.SmsBanner(
            isVisible = showSmsBanner,
            onSend = {
                showSmsBanner = false
                // Logic for mass SMS has been replaced with per-employee logic as per requirements.
                // It is better to rely on auto SMS for per-employee. If they click this banner, we could send for all approved today.
            },
            onDismiss = { showSmsBanner = false },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/java/com/example/ui/screens/attendance/AttendanceScreen.kt', 'w') as f:
        f.write(content)
    print("AttendanceScreen modified")
else:
    print("Target in AttendanceScreen not found")

# 2. Modify SmsBanner.kt to animate from bottom
with open('app/src/main/java/com/example/ui/components/SmsBanner.kt', 'r') as f:
    content = f.read()

target_anim = """    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it - 100 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it - 100 }) + fadeOut(),"""

replacement_anim = """    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it + 100 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it + 100 }) + fadeOut(),"""

if target_anim in content:
    content = content.replace(target_anim, replacement_anim)
    with open('app/src/main/java/com/example/ui/components/SmsBanner.kt', 'w') as f:
        f.write(content)
    print("SmsBanner modified")
else:
    print("Target in SmsBanner not found")

