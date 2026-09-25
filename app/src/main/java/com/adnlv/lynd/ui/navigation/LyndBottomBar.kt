package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.adnlv.lynd.util.HapticFeedbackHelper

@Composable
fun LyndBottomBar(
    navController: NavController,
    currentRoute: String?,
    bottomTabs: List<Screen> = listOf(Screen.Overview, Screen.Payouts, Screen.Holdings)
) {
    val view = LocalView.current
    val navBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    NavigationBar(
        modifier = Modifier.drawWithContent {
            drawContent()
            val strokeWidth = 1.dp.toPx()
            drawLine(
                color = navBorderColor,
                start = Offset(0f, strokeWidth / 2),
                end = Offset(size.width, strokeWidth / 2),
                strokeWidth = strokeWidth
            )
        }
    ) {
        bottomTabs.forEach { screen ->
            val selected = currentRoute == screen.route
            val pillBorderProgress by animateFloatAsState(
                targetValue = if (selected) 1f else 0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "pillBorderProgress"
            )
            val pillBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            NavigationBarItem(
                selected = selected,
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
                },
                icon = {
                    val iconModifier = Modifier.drawWithContent {
                        drawContent()
                        if (pillBorderProgress > 0f) {
                            val strokeWidth = 1.dp.toPx()
                            val pillWidth = 56.dp.toPx() * pillBorderProgress
                            val pillHeight = 32.dp.toPx()
                            val left = (size.width - pillWidth) / 2f
                            val top = (size.height - pillHeight) / 2f
                            if (pillWidth >= strokeWidth && pillHeight >= strokeWidth) {
                                val insetLeft = left + strokeWidth / 2f
                                val insetTop = top + strokeWidth / 2f
                                val insetWidth = pillWidth - strokeWidth
                                val insetHeight = pillHeight - strokeWidth
                                val radius = insetHeight / 2f
                                drawRoundRect(
                                    color = pillBorderColor.copy(alpha = pillBorderColor.alpha * pillBorderProgress),
                                    topLeft = Offset(insetLeft, insetTop),
                                    size = Size(insetWidth, insetHeight),
                                    cornerRadius = CornerRadius(radius, radius),
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }
                    }
                    when (screen) {
                        Screen.Overview -> Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = screen.title,
                            modifier = iconModifier
                        )
                        Screen.Payouts -> Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = screen.title,
                            modifier = iconModifier
                        )
                        Screen.Holdings -> Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = screen.title,
                            modifier = iconModifier
                        )
                        else -> {}
                    }
                },
                label = { Text(screen.title) }
            )
        }
    }
}
