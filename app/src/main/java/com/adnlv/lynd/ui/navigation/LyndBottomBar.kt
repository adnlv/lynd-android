package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.adnlv.lynd.util.HapticFeedbackHelper

@Composable
fun LyndBottomBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    bottomTabs: List<Screen> = listOf(Screen.Overview, Screen.Planner, Screen.Payouts, Screen.Holdings)
) {
    val view = LocalView.current
    val navBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawWithContent {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.forEach { screen ->
                val selected = currentRoute == screen.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
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
                            contentDescription = screen.title
                        )
                        Screen.Planner -> Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = screen.title
                        )
                        Screen.Payouts -> Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = screen.title
                        )
                        Screen.Holdings -> Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = screen.title
                        )
                    }
                    Text(screen.title)
                }
            }
        }
    }
}
