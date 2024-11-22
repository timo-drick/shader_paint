package de.drick.compose.shaderpaint

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.window.singleWindowApplication
import de.drick.compose.shaderpaint.de.drick.compose.shaderpaint.MainScreenHotReload

fun main() = singleWindowApplication {
    MaterialTheme {
        MainScreenHotReload()
    }
}

