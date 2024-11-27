package de.drick.compose.shaderpaint.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle

private val lightPrimaryColor = Color(0xFFFAB24A)
private val darkPrimaryColor = Color(0xFF7D4433)
private val textColorOnLight = Color.Black
private val textColorOnDark = Color.White

private val primary = Color(0xFFF49F0D)
private val primaryVariant = Color(0xFFFABE5D)
private val activeInverted = Color.White

private val secondary = Color(0xFF894E34)
private val secondaryVariant = Color(0xFF915E36)

private val foreground = Color(0xFF894E34)

private val background = Color(0xFFFEEED2)
private val backgroundVariant = Color(0xFFFAC367)


val logoColorLight = Color(0xFFFF9E00)
val logoColorDark = secondary

val shaderBackground = backgroundVariant
val shaderPrimary = primary

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = darkPrimaryColor,
    secondary = secondary,
    tertiary = darkPrimaryColor,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Immutable
data class CodeStyle(
    val simple: SpanStyle,
    val value: SpanStyle,
    val keyword: SpanStyle,
    val punctuation: SpanStyle,
    val function: SpanStyle,
    val comment: SpanStyle
)

@Composable
fun MaterialTheme.codeStyle() = if (isSystemInDarkTheme())
    CodeStyle(
        simple = SpanStyle(Color(0xFF000000)),
        value = SpanStyle(Color(0xFF4A86E8)),
        keyword = SpanStyle(Color(0xFF000080)),
        punctuation = SpanStyle(Color(0xFFA1A1A1)),
        function = SpanStyle(Color(0xFFBBB529)),
        comment = SpanStyle(Color(0xFF808080))
    )
else
    CodeStyle(
        simple = SpanStyle(Color(0xFFA9B7C6)),
        value = SpanStyle(Color(0xFF6897BB)),
        keyword = SpanStyle(Color(0xFFCC7832)),
        punctuation = SpanStyle(Color(0xFFA1C17E)),
        function = SpanStyle(fontStyle = FontStyle.Italic),
        comment = SpanStyle(Color(0xFF808080))
    )