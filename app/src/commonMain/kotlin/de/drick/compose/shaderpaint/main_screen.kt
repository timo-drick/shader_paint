

package de.drick.compose.shaderpaint

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.shaderpaint.theme.AppTheme
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

@Composable
fun Preview() {
    AppTheme {
        MainScreen(persistentWorld)
    }
}

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
                    selected = shapeSelected,
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
            val shaderCodeSource = remember(world.frameCounter) {
                world.generateShader()
            }
            ShaderCodePanel(
                source = shaderCodeSource,
                modifier = Modifier.height(300.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
fun ShaderPanel(
    world: World,
    onClick: (Offset) -> Unit,
    selected: Shape?,
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
        .pointerInput(selected) {
            if (selected != null) {
                var dragPos = Offset.Zero
                var dragTarget: Shape? = null
                detectDragGestures(
                    onDragStart = { offset ->
                        val shape = world.searchShape(offset)
                        if (shape == selected) {
                            dragTarget = shape
                            dragPos = offset
                        } else {
                            dragTarget = null
                            if (shape != null) {
                                onClick(offset)
                            }
                        }
                    },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        dragPos += dragAmount
                        mouseOffset = dragPos
                        dragTarget?.let { it.position = dragPos }
                        world.invalidate()
                    },
                    onDragEnd = {

                    },
                    onDragCancel = {

                    }
                )
            }
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
