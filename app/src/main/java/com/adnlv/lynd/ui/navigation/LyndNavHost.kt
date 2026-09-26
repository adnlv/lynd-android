package com.adnlv.lynd.ui.navigation

import androidx.compose.runtime.Composable
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

@Composable
fun LyndNavHost(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Overview.route,
        modifier = modifier
    ) {
        composable(Screen.Overview.route) {
            val overviewViewModel: OverviewViewModel = viewModel(
                factory = OverviewViewModel.provideFactory(
                    holdingDao = appContainer.database.holdingDao(),
                    bondDao = appContainer.database.bondDao(),
                    payoutDao = appContainer.database.payoutDao()
                )
            )
            OverviewScreen(viewModel = overviewViewModel)
        }
        composable(Screen.Planner.route) {
            val plannerViewModel: PlannerViewModel = viewModel(
                factory = PlannerViewModel.provideFactory(
                    holdingDao = appContainer.database.holdingDao(),
                    bondDao = appContainer.database.bondDao(),
                    payoutDao = appContainer.database.payoutDao()
                )
            )
            PlannerScreen(viewModel = plannerViewModel)
        }
        composable(Screen.Holdings.route) {
            val holdingsViewModel: HoldingsViewModel = viewModel(
                factory = HoldingsViewModel.provideFactory(
                    holdingDao = appContainer.database.holdingDao(),
                    nbuRepository = appContainer.nbuRepository,
                    bondDao = appContainer.database.bondDao()
                )
            )
            HoldingsScreen(viewModel = holdingsViewModel)
        }
        composable(Screen.Payouts.route) {
            val payoutsViewModel: PayoutsViewModel = viewModel(
                factory = PayoutsViewModel.provideFactory(appContainer.database.payoutDao())
            )
            PayoutsScreen(viewModel = payoutsViewModel)
        }
    }
}
