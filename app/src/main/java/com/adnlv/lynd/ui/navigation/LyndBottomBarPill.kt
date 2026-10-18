package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.ui.components.LyndGlassSurface
import com.adnlv.lynd.ui.theme.LyndLayoutDefaults

private const val IndicatorPressedScale = 0.90f
private val ContainerInnerPaddingHorizontal = 4.dp
private val ContainerInnerPaddingVertical = 4.dp
private val PillPaddingHorizontal = 2.dp
private val PillPaddingVertical = 1.dp

@Composable
fun LyndBottomBarPill(
    bottomTabs: List<Screen>,
    currentRoute: String?,
    onTabSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    onTabReselected: (Screen) -> Unit = {}
) {
    var isIndicatorPressed by remember { mutableStateOf(false) }
    LyndGlassSurface(
        shape = CircleShape,
        modifier = modifier.height(LyndLayoutDefaults.FloatingBarHeight)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = ContainerInnerPaddingHorizontal,
                    vertical = ContainerInnerPaddingVertical
                )
        ) {
            var pressedRoute by remember { mutableStateOf<String?>(null) }
            val activeTabRoute = Screen.resolveBottomTabRoute(currentRoute, bottomTabs)
            val displayedTabRoute = pressedRoute ?: activeTabRoute
            val tabCount = bottomTabs.size
            if (tabCount > 0) {
                val tabWidth = maxWidth / tabCount
                val selectedIndex = bottomTabs.indexOfFirst { it.route == displayedTabRoute }
                LyndBottomBarIndicator(
                    tabWidth = tabWidth,
                    selectedIndex = selectedIndex,
                    isPressed = isIndicatorPressed
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomTabs.forEach { screen ->
                    val selected = displayedTabRoute == screen.route
                    LyndBottomBarItem(
                        screen = screen,
                        selected = selected,
                        onPressStart = {
                            pressedRoute = screen.route
                            isIndicatorPressed = true
                        },
                        onRelease = {
                            pressedRoute = null
                            isIndicatorPressed = false
                            if (currentRoute == screen.route) {
                                onTabReselected(screen)
                            } else {
                                onTabSelected(screen)
                            }
                        },
                        onCancel = {
                            pressedRoute = null
                            isIndicatorPressed = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyndBottomBarIndicator(
    tabWidth: Dp,
    selectedIndex: Int,
    isPressed: Boolean = false
) {
    val isVisible = selectedIndex >= 0

    val pillOffset by animateDpAsState(
        targetValue = tabWidth * selectedIndex.coerceAtLeast(0),
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_offset"
    )

    val pillScale by animateFloatAsState(
        targetValue = if (isPressed) IndicatorPressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pill_scale"
    )

    val pillAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = LyndLayoutDefaults.DefaultAnimationDurationMs),
        label = "pill_alpha"
    )

    if (pillAlpha > 0f) {
        Box(
            modifier = Modifier
                .offset { IntOffset(x = pillOffset.roundToPx(), y = 0) }
                .width(tabWidth)
                .fillMaxHeight()
                .padding(
                    horizontal = PillPaddingHorizontal,
                    vertical = PillPaddingVertical
                )
                .graphicsLayer {
                    scaleX = pillScale
                    scaleY = pillScale
                    alpha = pillAlpha
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = CircleShape
                )
        )
    }
}
