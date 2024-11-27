package de.drick.compose.shaderpaint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
