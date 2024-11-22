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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import de.drick.compose.live_composable.HotReload
import org.intellij.lang.annotations.Language
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

interface Shape {
    val id: String
    var position: Offset
    var radius: Float
    var smoothness: Float
    var color: Color
    fun instance(posVar: String): String
}

fun Offset.toVector() = "vec2($x, $y)"

fun Float.toShader() = "%.2f".format(this)

fun Color.toVec3() = "vec3(${red.toShader()}, ${green.toShader()}, ${blue.toShader()})"

data class Circle(
    override val id: String,
    override var position: Offset,
    override var radius: Float,
    override var smoothness: Float = 10f,
    override var color: Color = Color.White,
): Shape {
    companion object {
        @Language("GLSL")
        fun shaderDefinition() = """
            float dCircle(in vec2 p, in float r, in float sharpness) {
                float d = length(p) - r + sharpness;
                return smoothstep(sharpness, 0.0, d);
            }
        """.replaceIndent()
    }
    override fun instance(posVar: String): String = """
        dCircle($posVar - ${position.toVector()}, ${radius.toShader()}, ${smoothness.toShader()}) * ${color.toVec3()}
    """.replaceIndent()
}

data class Triangle(
    override val id: String,
    override var position: Offset,
    override var radius: Float,
    override var smoothness: Float = 10f,
    override var color: Color = Color.White,
): Shape {
    companion object {
        @Language("GLSL")
        fun shaderDefinition() = """
            float sdEquilateralTriangle(in vec2 p, in float r, in float sharpness) {
                const float k = sqrt(3.0);
                p.x = abs(p.x) - r;
                p.y = p.y + r/k;
                if( p.x+k*p.y>0.0 ) p = vec2(p.x-k*p.y,-k*p.x-p.y)/2.0;
                p.x -= clamp( p.x, -2.0*r, 0.0 );
                float d = -length(p)*sign(p.y);
                return smoothstep(sharpness, 0.0, d);
            }
        """.replaceIndent()
    }
    override fun instance(posVar: String): String = """
        sdEquilateralTriangle($posVar - ${position.toVector()}, ${radius.toShader()}, ${smoothness.toShader()}) * ${color.toVec3()}
    """.replaceIndent()
}



class World {
    val shapeList = SnapshotStateList<Shape>()
        .apply {
            add(Circle("Circle 1", Offset(100f, 100f), 100f))
        }

    var frameCounter by mutableStateOf(0)
        private set

    fun invalidate() {
        frameCounter++
    }

    fun addShape(shape: Shape) {
        shapeList.add(shape)
        invalidate()
    }

    @Language("AGSL")
    private fun mainShader(
        shapeInstances: List<Shape>
    ): String {
        val definitions = buildString {
            append(Circle.shaderDefinition())
            append(Triangle.shaderDefinition())
        }
        val instances = buildString {
            shapeInstances.forEach { shape ->
                append("c += ")
                append(shape.instance("p"))
                appendLine(";")
            }
        }
        return """
            $definitions
            vec4 main(vec2 fragCoord) {
                vec2 p = fragCoord;
                vec3 c = vec3(0.0);
                $instances
                return vec4(c, 1);
            }    
        """.trimIndent()
    }
    fun searchShape(offset: Offset): Shape? {
        return shapeList
            .map { Pair(it, (it.position - offset).getDistance()) }
            .filter { it.second < it.first.radius + 20f}
            .minByOrNull { it.second }
            ?.first
    }

    fun generateShader() = mainShader(shapeList)
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
        println(shaderCode)
        RuntimeEffect.makeForShader(shaderCode)
    }
    Spacer(
        modifier = modifier
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

@HotReload
@Composable
fun MainScreen() {
    val world = remember {
        World()
    }
    var newShapeSelected by remember { mutableStateOf<Shape?>(null) }
    var shapeSelected by remember { mutableStateOf<Shape?>(world.shapeList.firstOrNull()) }

    var shapeCounter by remember { mutableStateOf(world.shapeList.size) }

    val pointerIcon = remember(newShapeSelected) {
        if (newShapeSelected != null)
            PointerIcon.Crosshair
        else
            PointerIcon.Default
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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
                    }
                }
            }
        )
        Column(
            modifier = Modifier.padding(8.dp).fillMaxHeight().width(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        shapeCounter++
                        val id = "Circle $shapeCounter"
                        newShapeSelected = Circle(id, Offset.Zero, 100f, color = Color.Red)
                    }
                ) {
                    Icon(imageVector = Icons.Default.Circle, contentDescription = null)
                }
                Button(
                    onClick = {
                        shapeCounter++
                        val id = "Triangle $shapeCounter"
                        newShapeSelected = Triangle(id, Offset.Zero, 100f)
                    }
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                }
            }
            shapeSelected?.let {
                Spacer(modifier = Modifier.height(4.dp).fillMaxWidth().background(Color.Black))
                ShapeOptionsView(it, onChange = { world.invalidate() })
            }
            Spacer(modifier = Modifier.height(4.dp).fillMaxWidth().background(Color.Black))
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
        var state by remember { mutableStateOf(shape.radius) }
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
        var state by remember { mutableStateOf(shape.smoothness) }
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