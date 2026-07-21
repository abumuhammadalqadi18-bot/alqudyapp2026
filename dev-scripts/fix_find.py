import re

with open('app/src/test/java/com/example/qadi/WageAndBalanceTest.kt', 'r') as f:
    content = f.read()

content = content.replace('wageRepo.getRecordsInRange(0L, 2000L).first().find', 'wageRepo.getRecordsInRange(0L, 2000L).first { it.isNotEmpty() }.find')

with open('app/src/test/java/com/example/qadi/WageAndBalanceTest.kt', 'w') as f:
    f.write(content)

