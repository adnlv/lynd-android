package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.adnlv.lynd.util.HapticFeedbackHelper

private val NeutralGray = Color(0xFF8E8E93)
private val TabBarHeight = 50.dp
private val BlurRadius = 20.dp
private val IconSize = 24.dp
private val ItemSpacing = 2.dp

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
            .drawWithContent {
                drawContent()
                val strokeWidth = 0.5.dp.toPx()
                drawLine(
                    color = navBorderColor,
                    start = Offset(0f, strokeWidth / 2f),
                    end = Offset(size.width, strokeWidth / 2f),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(BlurRadius)
                .background(backgroundColor)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(TabBarHeight),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { screen ->
                val selected = currentRoute == screen.route
                val itemColor = if (selected) MaterialTheme.colorScheme.primary else NeutralGray

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
                            imageVector = Icons.Default.TrendingUp,
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
