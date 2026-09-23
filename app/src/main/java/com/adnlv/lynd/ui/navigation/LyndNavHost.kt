package com.adnlv.lynd.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.adnlv.lynd.AppContainer
import com.adnlv.lynd.ui.addholding.AddHoldingScreen
import com.adnlv.lynd.ui.addholding.AddHoldingViewModel
import com.adnlv.lynd.ui.holdings.HoldingsScreen
import com.adnlv.lynd.ui.holdings.HoldingsViewModel
import com.adnlv.lynd.ui.overview.OverviewScreen
import com.adnlv.lynd.ui.overview.OverviewViewModel
import com.adnlv.lynd.ui.payouts.PayoutsScreen
import com.adnlv.lynd.ui.payouts.PayoutsViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
