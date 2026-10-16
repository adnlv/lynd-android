package com.adnlv.lynd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adnlv.lynd.ui.navigation.LyndBottomBar
import com.adnlv.lynd.ui.navigation.LyndNavHost
import com.adnlv.lynd.ui.navigation.Screen
import com.adnlv.lynd.ui.navigation.rememberBottomBarActionState
import com.adnlv.lynd.ui.theme.LocalLyndHazeState
import com.adnlv.lynd.ui.theme.LyndTheme
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as LyndApplication).container

        setContent {
            LyndTheme {
                MainApp(appContainer = appContainer)
            }
        }
    }
}

@Composable
fun MainApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomBarActionState = rememberBottomBarActionState()
    val hazeState = rememberHazeState()

    val bottomTabs = Screen.defaultBottomTabs
    val showBottomBar = Screen.shouldShowBottomBar(currentRoute, bottomTabs)

    CompositionLocalProvider(LocalLyndHazeState provides hazeState) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.statusBars
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                LyndNavHost(
                    navController = navController,
                    appContainer = appContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState),
                    bottomBarActionState = bottomBarActionState
                )
                if (showBottomBar) {
                    LyndBottomBar(
                        navController = navController,
                        currentRoute = currentRoute,
                        bottomTabs = bottomTabs,
                        action = bottomBarActionState.actionForRoute(currentRoute),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
