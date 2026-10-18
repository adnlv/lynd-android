package com.adnlv.lynd.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import com.adnlv.lynd.ui.theme.LocalLyndHazeState
import com.adnlv.lynd.ui.theme.LyndGlassDefaults
import com.adnlv.lynd.ui.theme.rememberLyndGlassStyle
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * Draws a floating surface with a blurred backdrop.
 * Use this for every element that floats above screen content.
 */
@Composable
fun LyndGlassSurface(
    shape: Shape,
    modifier: Modifier = Modifier,
    style: HazeBlurStyle = rememberLyndGlassStyle(),
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val hazeState = LocalLyndHazeState.current
    val colorScheme = MaterialTheme.colorScheme
    val backdropModifier = if (hazeState != null) {
        Modifier.hazeBlur(
            input = HazeInput.Sources(hazeState),
            style = style,
            performanceMode = LyndGlassDefaults.PerformanceMode
        )
    } else {
        Modifier.background(colorScheme.surface.copy(alpha = LyndGlassDefaults.FallbackAlpha))
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = LyndGlassDefaults.ShadowElevation,
                shape = shape,
                spotColor = colorScheme.onSurface.copy(alpha = LyndGlassDefaults.SpotShadowAlpha),
                ambientColor = colorScheme.onSurface.copy(alpha = LyndGlassDefaults.AmbientShadowAlpha)
            )
            .clip(shape)
            .then(backdropModifier)
            .border(
                width = LyndGlassDefaults.BorderWidth,
                color = colorScheme.outlineVariant.copy(alpha = LyndGlassDefaults.BorderAlpha),
                shape = shape
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}
