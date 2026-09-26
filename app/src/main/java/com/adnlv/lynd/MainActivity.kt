package com.adnlv.lynd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adnlv.lynd.ui.navigation.LyndBottomBar
import com.adnlv.lynd.ui.navigation.LyndNavHost
import com.adnlv.lynd.ui.navigation.Screen
import com.adnlv.lynd.ui.theme.LyndTheme

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

    val bottomTabs = listOf(Screen.Overview, Screen.Planner, Screen.Payouts, Screen.Holdings)
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                LyndBottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    bottomTabs = bottomTabs
                )
            }
        }
    ) { innerPadding ->
        LyndNavHost(
            navController = navController,
            appContainer = appContainer,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        )
    }
}
