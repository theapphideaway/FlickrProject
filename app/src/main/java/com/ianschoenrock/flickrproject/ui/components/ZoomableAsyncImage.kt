package com.ianschoenrock.flickrproject.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.widthIn
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch

@Composable
fun ZoomableAsyncImage(
    imageRequest: ImageRequest,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    minScale: Float = 1f,
    maxScale: Float = 5f
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    val nested = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (scale.value > 1f) available else Offset.Zero
            }
        }
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale.value * zoomChange).coerceIn(minScale, maxScale)
        scope.launch { scale.snapTo(newScale) }

        if (newScale > 1f) {
            scope.launch { offsetX.snapTo(offsetX.value + panChange.x) }
            scope.launch { offsetY.snapTo(offsetY.value + panChange.y) }
        } else {
            scope.launch { offsetX.snapTo(0f); offsetY.snapTo(0f) }
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .nestedScroll(nested)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            val target = if (scale.value < 2f) 2f else 1f
                            scale.animateTo(target)
                            if (target == 1f) {
                                offsetX.animateTo(0f)
                                offsetY.animateTo(0f)
                            }
                        }
                    }
                )
            }
            .transformable(transformState)
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 250.dp, max = 500.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    translationX = offsetX.value
                    translationY = offsetY.value
                },
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center
        )
    }
}
