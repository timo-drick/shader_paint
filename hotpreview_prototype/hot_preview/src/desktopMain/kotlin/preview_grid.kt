package de.drick.compose.hot_preview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import de.drick.compose.hotpreview.HotPreview
import de.drick.compose.live_composable.HotPreviewData

@Composable
fun PreviewItem(name: String, annotation: HotPreview, image: RenderedImage?) {
    val borderStroke = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    Column(Modifier) {
        val postFix = if (annotation.name.isNotBlank()) " - ${annotation.name}" else ""
        Text("$name $postFix")
        Spacer(Modifier.height(8.dp))
        if (image != null) {
            Image(
                modifier = Modifier.size(image.size).border(borderStroke),
                bitmap = image.image,
                contentScale = ContentScale.Fit,
                contentDescription = "Preview of $name"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreviewGridPanel(
    hotPreviewList: List<HotPreviewData>
) {
    val stateVertical = rememberScrollState()
    val stateHorizontal = rememberScrollState()
    val scrollbarPadding = 16.dp
    val scrollbarStyle = ScrollbarStyle(
        minimalHeight = 16.dp,
        thickness = 12.dp,
        shape = RoundedCornerShape(4.dp),
        hoverDurationMillis = 300,
        unhoverColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        hoverColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.50f)
    )
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .padding(scrollbarPadding)
                .verticalScroll(stateVertical)
                //.horizontalScroll(stateHorizontal)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hotPreviewList.forEach { preview ->
                    preview.function.annotation.forEachIndexed { index, annotation ->
                        PreviewItem(preview.function.name, annotation, preview.image.getOrNull(index))
                    }
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(stateVertical),
            style = scrollbarStyle
        )
        /*HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(end = scrollbarPadding),
            adapter = rememberScrollbarAdapter(stateHorizontal),
            style = scrollbarStyle
        )*/
    }
}