import re

with open("app/src/main/java/com/example/ui/theme/Type.kt", "r") as f:
    content = f.read()

# Add imports for Font and FontFamily
imports = """import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import com.example.R"""
content = content.replace("import androidx.compose.ui.text.font.FontFamily", imports)

font_family = """val ArabicFontFamily = FontFamily(
    Font(R.font.cairo_regular, FontWeight.Normal),
    Font(R.font.cairo_semibold, FontWeight.SemiBold),
    Font(R.font.cairo_bold, FontWeight.Bold)
)"""
content = content.replace("val ArabicFontFamily = FontFamily.SansSerif", font_family)

with open("app/src/main/java/com/example/ui/theme/Type.kt", "w") as f:
    f.write(content)
