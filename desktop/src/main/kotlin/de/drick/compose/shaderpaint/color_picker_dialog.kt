package de.drick.compose.shaderpaint.de.drick.compose.shaderpaint

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

private val Int.hex get() = this.toString(16).padStart(2, '0')

private fun Float.toHex2() = (this * 255).toInt().toString(16).padStart(2, '0').uppercase()
private fun Color.toHexString() = "#${alpha.toHex2()}${red.toHex2()}${green.toHex2()}${blue.toHex2()}"

@Composable
fun ColorChangeButton(
    color: Color,
    onSelected: (Color?) -> Unit
) {
    println("Color = $color - ${color.toHexString()}")
    var dialogVisible by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                dialogVisible = true
            }
        ) {
            Text(color.toHexString())
            Spacer(
                Modifier
                    .background(color)
                    .size(48.dp)
            )
        }
    }
    if (dialogVisible) {
        Dialog(
            onDismissRequest = {
                dialogVisible = false
            }
        ) {
            ColorChooser(
                initialColor = color,
                onSelected = { selectedColor ->
                    dialogVisible = false
                    onSelected(selectedColor)
                }
            )
        }
    }
}

@Composable
fun ColorChooser(
    initialColor: Color,
    onSelected: (Color?) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large
    ) {
        val controller = rememberColorPickerController()
        LaunchedEffect(Unit) {
            controller.selectByColor(initialColor, false)
        }
        val selectedColor = controller.selectedColor.value
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(224.dp)
                    .padding(10.dp),
                controller = controller,
            )
            AlphaSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(36.dp),
                controller = controller,
            )
            BrightnessSlider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(36.dp),
                controller = controller,
            )
            SelectionContainer {
                val hexCode = remember(selectedColor) {
                    selectedColor.toHexString()
                }
                Text(
                    text = hexCode,
                    fontSize = 32.sp
                )
            }
            AlphaTile(
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        onSelected(selectedColor)
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .padding(bottom = 8.dp),
                controller = controller
            )
        }
    }
}