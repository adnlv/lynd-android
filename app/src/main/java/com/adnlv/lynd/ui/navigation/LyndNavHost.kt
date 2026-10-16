package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.adnlv.lynd.AppContainer
import com.adnlv.lynd.ui.holdings.HoldingsScreen
import com.adnlv.lynd.ui.holdings.HoldingsViewModel
import com.adnlv.lynd.ui.overview.OverviewScreen
import com.adnlv.lynd.ui.overview.OverviewViewModel
import com.adnlv.lynd.ui.payouts.PayoutsScreen
import com.adnlv.lynd.ui.payouts.PayoutsViewModel
import com.adnlv.lynd.ui.planner.PlannerScreen
import com.adnlv.lynd.ui.planner.PlannerViewModel
import kotlinx.coroutines.launch

@Composable
fun LyndNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    bottomBarActionState: BottomBarActionState? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Overview.route,
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        predictivePopEnterTransition = { EnterTransition.None },
        predictivePopExitTransition = { ExitTransition.None }
    ) {
        composable(Screen.Overview.route) {
            val listState = rememberLazyListState()
            RegisterScrollToTopHandler(
                route = Screen.Overview.route,
                bottomBarActionState = bottomBarActionState,
                listState = listState
            )
            val overviewViewModel: OverviewViewModel = viewModel(
                factory = OverviewViewModel.provideFactory(
                    holdingDao = appContainer.database.holdingDao(),
                    bondDao = appContainer.database.bondDao(),
                    payoutDao = appContainer.database.payoutDao()
                )
            )
            OverviewScreen(
                viewModel = overviewViewModel,
                listState = listState
            )
        }
        composable(Screen.Planner.route) {
            val listState = rememberLazyListState()
            RegisterScrollToTopHandler(
                route = Screen.Planner.route,
                bottomBarActionState = bottomBarActionState,
                listState = listState
            )
            val plannerViewModel: PlannerViewModel = viewModel(
                factory = PlannerViewModel.provideFactory(
                    holdingDao = appContainer.database.holdingDao(),
                    bondDao = appContainer.database.bondDao(),
                    payoutDao = appContainer.database.payoutDao()
                )
            )
            PlannerScreen(
                viewModel = plannerViewModel,
                listState = listState
            )
        }
        composable(Screen.Holdings.route) {
            val listState = rememberLazyListState()
            RegisterScrollToTopHandler(
                route = Screen.Holdings.route,
                bottomBarActionState = bottomBarActionState,
                listState = listState
            )
            val holdingsViewModel: HoldingsViewModel = viewModel(
                factory = HoldingsViewModel.provideFactory(
                    holdingDao = appContainer.database.holdingDao(),
                    nbuRepository = appContainer.nbuRepository,
                    bondDao = appContainer.database.bondDao()
                )
            )
            HoldingsScreen(
                viewModel = holdingsViewModel,
                listState = listState,
                onActionChange = { action ->
                    bottomBarActionState?.setAction(Screen.Holdings.route, action)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Payouts.route) {
            val listState = rememberLazyListState()
            RegisterScrollToTopHandler(
                route = Screen.Payouts.route,
                bottomBarActionState = bottomBarActionState,
                listState = listState
            )
            val payoutsViewModel: PayoutsViewModel = viewModel(
                factory = PayoutsViewModel.provideFactory(appContainer.database.payoutDao())
            )
            PayoutsScreen(
                viewModel = payoutsViewModel,
                listState = listState,
                onActionChange = { action ->
                    bottomBarActionState?.setAction(Screen.Payouts.route, action)
                },
                onNavigateToHoldings = {
                    navController.navigate(Screen.Holdings.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun RegisterScrollToTopHandler(
    route: String,
    bottomBarActionState: BottomBarActionState?,
    listState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(route, bottomBarActionState, listState) {
        bottomBarActionState?.setReselectHandler(route) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
        onDispose {
            bottomBarActionState?.clearReselectHandler(route)
        }
    }
}
