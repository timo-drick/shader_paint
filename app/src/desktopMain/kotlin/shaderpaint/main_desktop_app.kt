package shaderpaint

import androidx.compose.runtime.remember
import androidx.compose.ui.window.singleWindowApplication
import de.drick.compose.shaderpaint.MainScreen
import de.drick.compose.shaderpaint.World
import de.drick.compose.shaderpaint.theme.AppTheme

fun main() = singleWindowApplication(
    title = "Shader PainT"
) {
    AppTheme(darkTheme = true) {
        MainScreen(world = remember { World() })
    }
}

