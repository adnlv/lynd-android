package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.adnlv.lynd.ui.theme.LyndTheme
import com.adnlv.lynd.util.HapticFeedbackHelper

private val ContainerHorizontalMargin = 16.dp
private val ContainerBottomMargin = 4.dp
private val ActionButtonSpacing = 8.dp

@Composable
fun LyndBottomBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    bottomTabs: List<Screen> = listOf(Screen.Overview, Screen.Planner, Screen.Payouts, Screen.Holdings),
    action: BottomBarAction = BottomBarAction.defaultForRoute(currentRoute)
) {
    val view = LocalView.current
    val scrimColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        scrimColor.copy(alpha = 0.5f),
                        scrimColor
                    )
                )
            )
            .navigationBarsPadding()
            .padding(
                start = ContainerHorizontalMargin,
                end = ContainerHorizontalMargin,
                bottom = ContainerBottomMargin
            ),
        horizontalArrangement = Arrangement.spacedBy(ActionButtonSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LyndBottomBarPill(
            bottomTabs = bottomTabs,
            currentRoute = currentRoute,
            onTabSelected = { screen ->
                HapticFeedbackHelper.vibratePageSwitch(view)
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier.weight(1f)
        )

        LyndContextualActionButton(action = action)
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun LyndBottomBarPreview() {
    LyndTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LyndBottomBar(
                navController = rememberNavController(),
                currentRoute = Screen.Overview.route
            )
        }
    }
}
