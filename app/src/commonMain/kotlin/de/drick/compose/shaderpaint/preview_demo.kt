package de.drick.compose.shaderpaint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.shaderpaint.theme.AppTheme

val colorCircle1 = Color(0xffd02c2c)
val colorCircle2 = Color(0xff1a5a4e)
val colorTriangle1 = Color(0xffb927bd)

val world = World().also { world ->
    val circle1 = Circle(
        id = "C1",
        position = Offset(300f, 200f),
        radius = 150f,
        smoothness = 100f,
        color = colorCircle1
    )
    world.addShape(circle1)
    val circle2 = Circle(
        id = "C2",
        position = Offset(600f, 400f),
        radius = 200f,
        smoothness = 100f,
        color = colorCircle2
    )
    world.addShape(circle2)
}

@HotPreview(widthDp = 700, heightDp = 500)
@Composable
fun PreviewShader() {
    AppTheme {
        ShaderPanel(
            modifier = Modifier.fillMaxSize(),
            world = world,
            onClick = {},
            selected = null
        )
    }
}

//@HotPreview(name = "Normal", fontScale = 0.8f)
@HotPreview(name = "light", darkMode = false)
@Composable
fun PreviewCode() {
    AppTheme {
        Surface {
            ShaderCodePanel(
                source = world.generateShader(),
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
