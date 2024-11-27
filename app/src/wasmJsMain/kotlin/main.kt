import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import de.drick.compose.shaderpaint.MainScreen
import de.drick.compose.shaderpaint.World
import de.drick.compose.shaderpaint.theme.AppTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        canvasElementId = "ComposeTarget"
    ) {
        AppTheme(darkTheme = true) {
            MainScreen(world = remember { World() })
        }
    }
}