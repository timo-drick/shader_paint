package de.drick.compose.shaderpaint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.shaderpaint.theme.AppTheme

val triangleColor = Color(0xff1d4e40)

private val world = World().apply {

    val shape = Circle(
        id = "1",
        position = Offset(500f,500f),
        radius = 250f,
        smoothness = 300f,
        color = triangleColor
    )
    addShape(
        shape
    )
}

@HotPreview(widthDp = 500, heightDp = 500)
@Composable
fun PreviewShaderPanel() {
    AppTheme {
        ShaderPanel(
            modifier = Modifier.fillMaxSize(),
            world = world,
            selected = null,
            onClick = {}
        )
    }
}

@HotPreview(name = "dark", darkMode = true, fontScale = 1.5f)
@HotPreview(name = "light", darkMode = false)
@Composable
fun PreviewDemoCodePanel() {
    AppTheme {
        Surface {
            ShaderCodePanel(
                source = world.generateShader(),
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
