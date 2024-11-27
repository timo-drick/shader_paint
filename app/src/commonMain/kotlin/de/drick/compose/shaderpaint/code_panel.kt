package de.drick.compose.shaderpaint

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.drick.compose.shaderpaint.theme.codeStyle
import kotlin.text.Regex.Companion.fromLiteral

@Composable
fun ShaderCodePanel(
    world: World,
    modifier: Modifier = Modifier
) {
    val shaderCodeLines = remember(world.frameCounter) {
        world.generateShader().lines()
    }
    LazyColumn(modifier) {
        itemsIndexed(shaderCodeLines) { index, line ->
            Row() {
                LineNumber(
                    number = index.toString(),
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    text = codeString(line),
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun LineNumber(number: String, modifier: Modifier) =
    Text(
        text = number,
        fontSize = 16.sp,
        fontFamily = FontFamily.Monospace,
        color = LocalContentColor.current.copy(alpha = 0.30f),
        modifier = modifier.padding(start = 12.dp)
    )

private val punctuationSymbols = listOf(
    ":", ";", "=",
    "\"",
    "[",
    "]",
)

private val keywordSymbols = listOf(
    "float",
    "vec2",
    "vec3",
    "vec4",
    "in ",
    "out ",
    "uniform ",
    "return"
)

private val functionSymbols = listOf(
    "main"
)

@Composable
private fun codeString(str: String) = buildAnnotatedString {
    val codeStyle = MaterialTheme.codeStyle()
    withStyle(codeStyle.simple) {
        val strFormatted = str.replace("\t", "    ")
        append(strFormatted)
        punctuationSymbols.forEach { symbol ->
            addStyle(codeStyle.punctuation, strFormatted, symbol)
        }
        keywordSymbols.forEach { symbol ->
            addStyle(codeStyle.keyword, strFormatted, symbol)
        }
        functionSymbols.forEach { symbol ->
            addStyle(codeStyle.function, strFormatted, symbol)
        }
        addStyle(codeStyle.value, strFormatted, "true")
        addStyle(codeStyle.value, strFormatted, "false")
        addStyle(codeStyle.value, strFormatted, Regex("""(?<!\p{Alpha})[0-9\\.]+"""))
        addStyle(codeStyle.comment, strFormatted, Regex("^\\s*//.*"))

        // Keeps copied lines separated and fixes crash during selection:
        // https://partnerissuetracker.corp.google.com/issues/199919707
        append("\n")
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: String) {
    addStyle(style, text, fromLiteral(regexp))
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
    for (result in regexp.findAll(text)) {
        addStyle(style, result.range.first, result.range.last + 1)
    }
}
