package com.adnlv.lynd.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Overview : Screen("overview", "Overview")
    data object Planner : Screen("planner", "Planner")
    data object Payouts : Screen("payouts", "Payouts")
    data object Holdings : Screen("holdings", "Holdings")
}
