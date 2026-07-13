import re

def filter_value_change(content, var_name):
    # Find: onValueChange = { <var_name> = it }
    pattern = r'onValueChange\s*=\s*\{\s*' + var_name + r'\s*=\s*it\s*\}'
    
    replacement = f"""onValueChange = {{ newValue ->
                        val filtered = newValue.replace(",", ".").replace("،", ".").replace("٫", ".")
                        if (filtered.count {{ it == '.' }} <= 1 && filtered.all {{ it.isDigit() || it == '.' }}) {{
                            {var_name} = filtered
                        }}
                    }}"""
    
    return re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/screens/employee/AddEditEmployeeScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()
content = filter_value_change(content, 'dailyWage')
with open('app/src/main/java/com/example/ui/screens/employee/AddEditEmployeeScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/withdraw/WithdrawScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()
content = filter_value_change(content, 'amountText')
with open('app/src/main/java/com/example/ui/screens/withdraw/WithdrawScreen.kt', 'w', encoding='utf-8') as f:
    f.write(content)

