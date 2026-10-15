package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.adnlv.lynd.util.HapticFeedbackHelper

private val FloatingBarHeight = 64.dp
private val BlurRadius = 20.dp
private val IconSize = 22.dp
private val ItemSpacing = 2.dp
private val ContainerHorizontalMargin = 20.dp
private val ContainerBottomMargin = 12.dp
private val ContainerInnerPaddingHorizontal = 8.dp
private val ContainerInnerPaddingVertical = 6.dp
private val PillPaddingHorizontal = 4.dp
private val PillPaddingVertical = 2.dp

@Composable
fun LyndBottomBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    bottomTabs: List<Screen> = listOf(Screen.Overview, Screen.Planner, Screen.Payouts, Screen.Holdings)
) {
    val view = LocalView.current
    val navBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = ContainerHorizontalMargin,
                end = ContainerHorizontalMargin,
                bottom = ContainerBottomMargin
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FloatingBarHeight)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                    ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
                .clip(CircleShape)
                .border(
                    width = 0.5.dp,
                    color = navBorderColor,
                    shape = CircleShape
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(BlurRadius)
                    .background(backgroundColor)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = ContainerInnerPaddingHorizontal,
                        vertical = ContainerInnerPaddingVertical
                    )
            ) {
                val tabCount = bottomTabs.size
                if (tabCount > 0) {
                    val tabWidth = maxWidth / tabCount
                    val selectedIndex = bottomTabs.indexOfFirst { it.route == currentRoute }
                    val isVisible = selectedIndex >= 0

                    val pillOffset by animateDpAsState(
                        targetValue = tabWidth * selectedIndex.coerceAtLeast(0),
                        animationSpec = spring(
                            dampingRatio = 0.82f,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "pill_offset"
                    )

                    val pillAlpha by animateFloatAsState(
                        targetValue = if (isVisible) 1f else 0f,
                        animationSpec = tween(durationMillis = 200),
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
                                .graphicsLayer { alpha = pillAlpha }
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomTabs.forEach { screen ->
                        val selected = currentRoute == screen.route
                        val itemColor by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            animationSpec = tween(durationMillis = 200),
                            label = "tab_color"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .selectable(
                                    selected = selected,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Tab,
                                    onClick = {
                                        if (!selected) {
                                            HapticFeedbackHelper.vibratePageSwitch(view)
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            when (screen) {
                                Screen.Overview -> Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = screen.title,
                                    tint = itemColor,
                                    modifier = Modifier.size(IconSize)
                                )
                                Screen.Planner -> Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = screen.title,
                                    tint = itemColor,
                                    modifier = Modifier.size(IconSize)
                                )
                                Screen.Payouts -> Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = screen.title,
                                    tint = itemColor,
                                    modifier = Modifier.size(IconSize)
                                )
                                Screen.Holdings -> Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = screen.title,
                                    tint = itemColor,
                                    modifier = Modifier.size(IconSize)
                                )
                            }
                            Spacer(modifier = Modifier.height(ItemSpacing))
                            Text(
                                text = screen.title,
                                color = itemColor,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
