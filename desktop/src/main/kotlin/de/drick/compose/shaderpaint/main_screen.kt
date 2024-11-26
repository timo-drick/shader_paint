package de.drick.compose.shaderpaint.de.drick.compose.shaderpaint

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.drick.compose.shaderpaint.theme.codeStyle
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.text.Regex.Companion.fromLiteral


//@HotReload
@Composable
fun MainScreen(
    world: World
) {
    var newShapeSelected: Shape? by remember { mutableStateOf(null) }
    var shapeSelected: Shape? by remember { mutableStateOf(world.shapeList.firstOrNull()) }

    val pointerIcon = remember(newShapeSelected) {
        if (newShapeSelected != null)
            PointerIcon.Crosshair
        else
            PointerIcon.Default
    }
    Surface {
        Column {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                ShaderPanel(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .pointerHoverIcon(pointerIcon),
                    world = world,
                    onClick = { offset ->
                        newShapeSelected.let { shape ->
                            if (shape == null) {
                                shapeSelected = world.searchShape(offset)
                                println("Select: $shapeSelected")
                            } else {
                                println("Add shape")
                                shape.position = offset
                                world.addShape(shape)
                                newShapeSelected = null
                                shapeSelected = shape
                            }
                        }
                    }
                )
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxHeight().width(250.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 6.dp
                    ) {
                        ToolbarView(
                            modifier = Modifier.padding(8.dp),
                            world = world,
                            shapeSelected = shapeSelected,
                            newShapeSelected = { newShape ->
                                newShapeSelected = newShape
                            }
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 6.dp
                    ) {
                        LazyColumn(
                            Modifier.weight(1f),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(world.shapeList) { shape ->
                                ShapeItem(
                                    shape = shape,
                                    isSelected = shape.id == shapeSelected?.id
                                )
                            }
                        }
                    }
                }
            }
            ShaderCodePanel(
                world = world,
                modifier = Modifier.height(300.dp).fillMaxWidth()
            )
        }
    }
}

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


@Composable
fun ShaderPanel(
    world: World,
    onClick: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mouseOffset by remember { mutableStateOf(Offset.Zero) }
    val shaderCounter = world.frameCounter
    val effect = remember(shaderCounter) {
        val shaderCode = world.generateShader()
        RuntimeEffect.makeForShader(shaderCode)
    }
    Spacer(modifier
        .pointerInput(Unit) {
            detectTapGestures(onTap = onClick)
        }
        .pointerInput(Unit) {
            var dragPos = Offset.Zero
            var dragTarget: Shape? = null
            detectDragGestures(
                onDragStart = { offset ->
                    dragTarget = world.searchShape(offset)
                    dragPos = offset
                },
                onDrag = { change: PointerInputChange, dragAmount: Offset ->
                    dragPos += dragAmount
                    mouseOffset = dragPos
                    dragTarget?.let { it.position = dragPos }
                    world.invalidate()
                }
            )
        }.drawWithCache {
            val shader = RuntimeShaderBuilder(effect)
                .apply {
                    //Uniforms
                }
                .makeShader()
            val brush = ShaderBrush(shader)
            onDrawBehind {
                drawRect(brush)
            }
        }
    )
}

@Composable
fun ToolbarView(
    world: World,
    shapeSelected: Shape?,
    newShapeSelected: (Shape) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                val currentCount = world.shapeList.filterIsInstance<Circle>().size
                val id = "Circle ${currentCount + 1}"
                newShapeSelected(Circle(id, Offset.Zero, 100f, color = Color.Red))
            }
        ) {
            Icon(imageVector = Icons.Default.Circle, contentDescription = null)
        }
        Button(
            onClick = {
                val currentCount = world.shapeList.filterIsInstance<Triangle>().size
                val id = "Triangle ${currentCount + 1}"
                newShapeSelected(Triangle(id, Offset.Zero, 100f))
            }
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
        }
        shapeSelected?.let {
            Spacer(modifier = Modifier.height(4.dp).fillMaxWidth().background(Color.Black))
            ShapeOptionsView(it, onChange = { world.invalidate() })
        }
    }
}

@Composable
fun ShapeItem(
    shape: Shape,
    isSelected: Boolean
) {
    val background = if (isSelected) Color.Green else Color.Transparent
    Text(
        modifier = Modifier.fillMaxWidth().padding(8.dp).background(background),
        text = shape.id
    )
}

@Composable
fun ShapeOptionsView(
    shape: Shape,
    onChange: () -> Unit
) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        text = shape.id
    )
    Row {
        Text("radius")
        var state by remember(shape) { mutableStateOf(shape.radius) }
        Slider(
            value = state,
            onValueChange = {
                shape.radius = it
                state = it
                onChange()
            },
            valueRange = 0f..1000f
        )
    }
    Row {
        Text("Smoothness")
        var state by remember(shape) { mutableStateOf(shape.smoothness) }
        Slider(
            value = state,
            onValueChange = {
                shape.smoothness = it
                state = it
                onChange()
            },
            valueRange = 0f..200f
        )
    }
    ColorChangeButton(shape.color, onSelected = {
        it?.let { newColor ->
            shape.color = newColor
            onChange()
        }
    })
}