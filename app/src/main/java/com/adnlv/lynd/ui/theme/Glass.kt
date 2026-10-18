package com.adnlv.lynd.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect

/**
 * Holds the app-wide blur source. A null value means no source exists,
 * for example in previews. Glass surfaces then draw a solid background.
 */
val LocalLyndHazeState = staticCompositionLocalOf<HazeState?> { null }

object LyndGlassDefaults {
    val BlurRadius = 32.dp
    val BorderWidth = 0.5.dp
    val ShadowElevation = 8.dp
    val PerformanceMode: HazePerformanceMode = HazePerformanceMode.Balanced

    const val NoiseFactor = 0.12f
    const val LightTintAlpha = 0.83f
    const val DarkTintAlpha = 0.90f
    const val FallbackAlpha = 0.95f
    const val BorderAlpha = 0.35f
    const val SpotShadowAlpha = 0.20f
    const val AmbientShadowAlpha = 0.08f
    const val LuminanceThreshold = 0.5f
}

/**
 * Returns a thick, diffused blur style. The style is remembered,
 * so the blur node does not update on each recomposition.
 */
@Composable
fun rememberLyndGlassStyle(
    containerColor: Color = MaterialTheme.colorScheme.surface
): HazeBlurStyle = remember(containerColor) {
    val tintAlpha = if (containerColor.luminance() >= LyndGlassDefaults.LuminanceThreshold) {
        LyndGlassDefaults.LightTintAlpha
    } else {
        LyndGlassDefaults.DarkTintAlpha
    }
    HazeBlurStyle {
        backgroundColor(containerColor)
        blurRadius(LyndGlassDefaults.BlurRadius)
        noiseFactor(LyndGlassDefaults.NoiseFactor)
        colorEffects(listOf(HazeColorEffect.tint(containerColor.copy(alpha = tintAlpha))))
        fallbackColorEffect(
            HazeColorEffect.tint(containerColor.copy(alpha = LyndGlassDefaults.FallbackAlpha))
        )
    }
}
