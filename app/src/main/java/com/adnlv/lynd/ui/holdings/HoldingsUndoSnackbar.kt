package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun HoldingsUndoSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val totalSeconds = 3
    var remainingSeconds by remember(data) { mutableStateOf(totalSeconds) }
    val progress = remember(data) { Animatable(1f) }
    val swipeOffsetX = remember(data) { Animatable(0f) }

    LaunchedEffect(data) {
        launch {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            }
        }
        progress.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = totalSeconds * 1000,
                easing = LinearEasing
            )
        )
        data.dismiss()
    }

    val dismissThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .height(56.dp)
            .offset { IntOffset(swipeOffsetX.value.roundToInt(), 0) }
            .alpha(1f - (abs(swipeOffsetX.value) / (dismissThresholdPx * 2f)).coerceIn(0f, 1f))
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(data) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        coroutineScope.launch {
                            swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (abs(swipeOffsetX.value) > dismissThresholdPx) {
                                val target = if (swipeOffsetX.value > 0) dismissThresholdPx * 3 else -dismissThresholdPx * 3
                                swipeOffsetX.animateTo(target)
                                data.dismiss()
                            } else {
                                swipeOffsetX.animateTo(0f)
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            swipeOffsetX.animateTo(0f)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = 6.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        text = "${remainingSeconds}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                data.visuals.actionLabel?.let { label ->
                    TextButton(
                        onClick = { data.performAction() },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}
