package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

@Composable
fun LyndBottomBar(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    bottomTabs: List<Screen> = listOf(Screen.Overview, Screen.Planner, Screen.Payouts, Screen.Holdings)
) {
    val view = LocalView.current
    val scrimColor = MaterialTheme.colorScheme.surface
    val isPayoutsRoute = currentRoute == Screen.Payouts.route
    val endMargin = if (isPayoutsRoute) {
        ContainerHorizontalMargin + FloatingBarHeight + 8.dp
    } else {
        ContainerHorizontalMargin
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isPayoutsRoute) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scrimColor.copy(alpha = 0.5f),
                                scrimColor
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .navigationBarsPadding()
            .padding(
                start = ContainerHorizontalMargin,
                end = endMargin,
                bottom = ContainerBottomMargin
            )
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
            modifier = Modifier.fillMaxWidth()
        )
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
