package de.drick.compose.hot_preview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
                modifier = Modifier.width(image.size.width).aspectRatio(image.size.width / image.size.height).border(borderStroke),
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
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .padding(12.dp)
                .verticalScroll(stateVertical)
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
            adapter = rememberScrollbarAdapter(stateVertical)
        )
        /*HorizontalScrollbar(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(end = 12.dp),
            adapter = rememberScrollbarAdapter(stateHorizontal)
        )*/
    }
}