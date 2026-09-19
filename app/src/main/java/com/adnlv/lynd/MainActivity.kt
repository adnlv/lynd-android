package com.adnlv.lynd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adnlv.lynd.util.HapticFeedbackHelper
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.adnlv.lynd.ui.addholding.AddHoldingScreen
import com.adnlv.lynd.ui.addholding.AddHoldingViewModel
import com.adnlv.lynd.ui.holdings.HoldingsScreen
import com.adnlv.lynd.ui.holdings.HoldingsViewModel
import com.adnlv.lynd.ui.payouts.PayoutsScreen
import com.adnlv.lynd.ui.payouts.PayoutsViewModel
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

sealed class Screen(val route: String, val title: String) {
    data object Holdings : Screen("holdings", "Holdings")
    data object Payouts : Screen("payouts", "Payouts")
    data object AddHolding : Screen("add_holding", "Add Holding")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val view = LocalView.current

    val bottomTabs = listOf(Screen.Payouts, Screen.Holdings)
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { screen ->
                        val selected = currentRoute == screen.route
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
                                when (screen) {
                                    Screen.Holdings -> Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = screen.title
                                    )
                                    Screen.Payouts -> Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = screen.title
                                    )
                                    else -> {}
                                }
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Payouts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Holdings.route) {
                val holdingsViewModel: HoldingsViewModel = viewModel(
                    factory = HoldingsViewModel.provideFactory(
                        holdingDao = appContainer.database.holdingDao(),
                        nbuRepository = appContainer.nbuRepository,
                        bondDao = appContainer.database.bondDao()
                    )
                )
                val addHoldingViewModel: AddHoldingViewModel = viewModel(
                    factory = AddHoldingViewModel.provideFactory(
                        nbuRepository = appContainer.nbuRepository,
                        holdingDao = appContainer.database.holdingDao()
                    )
                )
                HoldingsScreen(
                    viewModel = holdingsViewModel,
                    addHoldingContent = { sheetState, holdingToEdit, onDismiss ->
                        LaunchedEffect(holdingToEdit) {
                            if (holdingToEdit == null) {
                                addHoldingViewModel.reset()
                            }
                        }
                        AddHoldingScreen(
                            viewModel = addHoldingViewModel,
                            onNavigateBack = onDismiss,
                            initialHolding = holdingToEdit,
                            sheetState = sheetState
                        )
                    }
                )
            }
            composable(Screen.Payouts.route) {
                val payoutsViewModel: PayoutsViewModel = viewModel(
                    factory = PayoutsViewModel.provideFactory(appContainer.database.payoutDao())
                )
                PayoutsScreen(viewModel = payoutsViewModel)
            }
            composable(Screen.AddHolding.route) {
                val addHoldingViewModel: AddHoldingViewModel = viewModel(
                    factory = AddHoldingViewModel.provideFactory(
                        nbuRepository = appContainer.nbuRepository,
                        holdingDao = appContainer.database.holdingDao()
                    )
                )
                AddHoldingScreen(
                    viewModel = addHoldingViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
