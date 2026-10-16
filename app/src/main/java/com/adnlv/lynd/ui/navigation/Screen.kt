package com.adnlv.lynd.ui.navigation

sealed class Screen(
    val route: String,
    val title: String,
    val parentRoute: String? = null
) {
    data object Overview : Screen("overview", "Overview")
    data object Planner : Screen("planner", "Planner")
    data object Payouts : Screen("payouts", "Payouts")
    data object Holdings : Screen("holdings", "Holdings", parentRoute = Payouts.route)

    companion object {
        val defaultBottomTabs: List<Screen> by lazy { listOf(Overview, Planner, Payouts) }

        fun fromRoute(route: String?): Screen? = when (route) {
            Overview.route -> Overview
            Planner.route -> Planner
            Payouts.route -> Payouts
            Holdings.route -> Holdings
            else -> null
        }

        fun resolveBottomTabRoute(
            currentRoute: String?,
            bottomTabs: List<Screen> = defaultBottomTabs
        ): String? {
            if (currentRoute == null) return null
            val tabRoutes = bottomTabs.map { it.route }.toSet()
            if (currentRoute in tabRoutes) return currentRoute
            val parent = fromRoute(currentRoute)?.parentRoute
            return if (parent in tabRoutes) parent else null
        }

        fun shouldShowBottomBar(
            currentRoute: String?,
            bottomTabs: List<Screen> = defaultBottomTabs
        ): Boolean = resolveBottomTabRoute(currentRoute, bottomTabs) != null
    }
}

