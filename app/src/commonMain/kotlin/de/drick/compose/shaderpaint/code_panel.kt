package de.drick.compose.shaderpaint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.shaderpaint.theme.AppTheme
import de.drick.compose.shaderpaint.theme.codeStyle
import kotlin.text.Regex.Companion.fromLiteral

@HotPreview("Dark", darkMode = true, fontScale = 1.0f)
@HotPreview(name = "Normal", darkMode = false)
@Composable
private fun PreviewShaderCodePanel() {
    val sampleCode = """
        float dCircle(in vec2 p, in float r, in float sharpness) {
            float d = length(p) - r + sharpness;
            return smoothstep(sharpness, 0.0, d);
        }
        
        vec4 main(vec2 fragCoord) {
            vec2 p = fragCoord;
            vec3 c = vec3(0.0);
            c += dCircle(p - vec2(100.0, 100.0), 100.0, 10.0) * vec3(1.0, 1.0, 1.0);

            return vec4(c, 1);
        }
    """.trimIndent()

    AppTheme {
        Surface {
            ShaderCodePanel(
                source = sampleCode,
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@HotPreview(widthDp = 400, heightDp = 300)
@Composable
fun PreviewShaderCodePanel2() {
    val sampleCode = """
        vec4 main(vec2 fragCoord) {
            vec2 p = fragCoord;
            vec3 c = vec3(0.0);
            c += dCircle(p - vec2(100.0, 100.0), 100.0, 10.0) * vec3(1.0, 1.0, 1.0);

            return vec4(c, 1);
        }
    """.trimIndent()
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface {
            ShaderCodePanel(
                source = sampleCode,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ShaderCodePanel(
    source: String,
    modifier: Modifier = Modifier
) {
    val shaderCodeLines = remember(source) {
        source.lines()
    }
    Box() {
        SelectionContainer {
            LazyColumn(modifier) {
                itemsIndexed(shaderCodeLines) { index, line ->
                    Row() {
                        LineNumber(
                            number = index.toString(),
                            modifier = Modifier.width(50.dp)
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
        val clipboardManager = LocalClipboardManager.current
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = {
                clipboardManager.setText(AnnotatedString(source))
            }
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy source")
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
