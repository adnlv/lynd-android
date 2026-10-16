package com.adnlv.lynd

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Tune
import com.adnlv.lynd.ui.navigation.BottomBarAction
import com.adnlv.lynd.ui.navigation.BottomBarActionState
import com.adnlv.lynd.ui.navigation.Screen
import com.adnlv.lynd.ui.navigation.icon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenNavigationTest {

    @Test
    fun overviewScreen_hasCorrectRouteAndTitle() {
        assertEquals("overview", Screen.Overview.route)
        assertEquals("Overview", Screen.Overview.title)
    }

    @Test
    fun payoutsScreen_hasCorrectRouteAndTitle() {
        assertEquals("payouts", Screen.Payouts.route)
        assertEquals("Payouts", Screen.Payouts.title)
    }

    @Test
    fun plannerScreen_hasCorrectRouteAndTitle() {
        assertEquals("planner", Screen.Planner.route)
        assertEquals("Planner", Screen.Planner.title)
    }

    @Test
    fun holdingsScreen_hasCorrectRouteAndTitle() {
        assertEquals("holdings", Screen.Holdings.route)
        assertEquals("Holdings", Screen.Holdings.title)
    }

    @Test
    fun screenIcons_matchExpectedVectors() {
        assertEquals(Icons.Default.Dashboard, Screen.Overview.icon())
        assertEquals(Icons.AutoMirrored.Filled.TrendingUp, Screen.Planner.icon())
        assertEquals(Icons.Default.DateRange, Screen.Payouts.icon())
        assertEquals(Icons.AutoMirrored.Filled.List, Screen.Holdings.icon())
    }

    @Test
    fun bottomBarAction_defaultForRoute_returnsDistinctActionForEachScreen() {
        val overviewAction = BottomBarAction.defaultForRoute(Screen.Overview.route)
        assertEquals(Icons.Default.Tune, overviewAction.icon)
        assertNull(overviewAction.label)
        assertEquals("Overview actions", overviewAction.contentDescription)

        val plannerAction = BottomBarAction.defaultForRoute(Screen.Planner.route)
        assertEquals(Icons.Default.FilterList, plannerAction.icon)
        assertNull(plannerAction.label)
        assertEquals("Planner actions", plannerAction.contentDescription)

        val payoutsAction = BottomBarAction.defaultForRoute(Screen.Payouts.route)
        assertEquals(Icons.Default.FilterList, payoutsAction.icon)
        assertNull(payoutsAction.label)
        assertEquals("Open filter menu", payoutsAction.contentDescription)

        val holdingsAction = BottomBarAction.defaultForRoute(Screen.Holdings.route)
        assertEquals(Icons.Default.Add, holdingsAction.icon)
        assertNull(holdingsAction.label)
        assertEquals("Holdings actions", holdingsAction.contentDescription)

        val nullAction = BottomBarAction.defaultForRoute(null)
        assertEquals(Icons.Default.Tune, nullAction.icon)
        assertNull(nullAction.label)
    }

    @Test
    fun bottomBarActionState_registersAndClearsActions() {
        val state = BottomBarActionState()
        val defaultPayouts = state.actionForRoute(Screen.Payouts.route)
        assertNull(defaultPayouts.label)
        assertEquals("Open filter menu", defaultPayouts.contentDescription)
        assertNull(defaultPayouts.menuContent)

        val customAction = BottomBarAction(
            icon = Icons.Default.FilterList,
            label = "Filter",
            contentDescription = "Custom payouts filter",
            menuContent = {}
        )
        state.setAction(Screen.Payouts.route, customAction)
        val resolved = state.actionForRoute(Screen.Payouts.route)
        assertEquals("Filter", resolved.label)
        assertEquals("Custom payouts filter", resolved.contentDescription)
        assertNotNull(resolved.menuContent)

        state.clearAction(Screen.Payouts.route)
        val cleared = state.actionForRoute(Screen.Payouts.route)
        assertNull(cleared.label)
        assertEquals("Open filter menu", cleared.contentDescription)
    }
}
