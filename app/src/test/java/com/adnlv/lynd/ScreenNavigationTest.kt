package com.adnlv.lynd

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import com.adnlv.lynd.ui.icons.Add
import com.adnlv.lynd.ui.icons.Close
import com.adnlv.lynd.ui.icons.Event
import com.adnlv.lynd.ui.icons.FilterList
import com.adnlv.lynd.ui.icons.Home
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.icons.PlannerReview
import com.adnlv.lynd.ui.icons.Settings
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
        assertEquals("payouts", Screen.Holdings.parentRoute)
    }

    @Test
    fun screen_defaultBottomTabs_containsExpectedScreens() {
        assertEquals(listOf(Screen.Overview, Screen.Planner, Screen.Payouts), Screen.defaultBottomTabs)
    }

    @Test
    fun screen_resolveBottomTabRoute_mapsChildToParentRoute() {
        assertEquals("overview", Screen.resolveBottomTabRoute(Screen.Overview.route))
        assertEquals("planner", Screen.resolveBottomTabRoute(Screen.Planner.route))
        assertEquals("payouts", Screen.resolveBottomTabRoute(Screen.Payouts.route))
        assertEquals("payouts", Screen.resolveBottomTabRoute(Screen.Holdings.route))
        assertEquals(null, Screen.resolveBottomTabRoute("unknown_route"))
        assertEquals(null, Screen.resolveBottomTabRoute(null))
    }

    @Test
    fun screen_shouldShowBottomBar_returnsTrueForTabsAndChildren() {
        org.junit.Assert.assertTrue(Screen.shouldShowBottomBar(Screen.Overview.route))
        org.junit.Assert.assertTrue(Screen.shouldShowBottomBar(Screen.Planner.route))
        org.junit.Assert.assertTrue(Screen.shouldShowBottomBar(Screen.Payouts.route))
        org.junit.Assert.assertTrue(Screen.shouldShowBottomBar(Screen.Holdings.route))
        org.junit.Assert.assertFalse(Screen.shouldShowBottomBar("unknown_route"))
        org.junit.Assert.assertFalse(Screen.shouldShowBottomBar(null))
    }

    @Test
    fun screenIcons_matchExpectedVectors() {
        assertEquals(MaterialSymbols.Home, Screen.Overview.icon())
        assertEquals(MaterialSymbols.PlannerReview, Screen.Planner.icon())
        assertEquals(MaterialSymbols.Event, Screen.Payouts.icon())
        assertEquals(Icons.AutoMirrored.Filled.List, Screen.Holdings.icon())
        assertEquals("close", MaterialSymbols.Close.name)
    }

    @Test
    fun bottomBarAction_defaultForRoute_returnsDistinctActionForEachScreen() {
        val overviewAction = BottomBarAction.defaultForRoute(Screen.Overview.route)
        assertEquals(MaterialSymbols.Settings, overviewAction.icon)
        assertNull(overviewAction.label)
        assertEquals("Overview actions", overviewAction.contentDescription)

        val plannerAction = BottomBarAction.defaultForRoute(Screen.Planner.route)
        assertEquals(MaterialSymbols.FilterList, plannerAction.icon)
        assertNull(plannerAction.label)
        assertEquals("Planner actions", plannerAction.contentDescription)

        val payoutsAction = BottomBarAction.defaultForRoute(Screen.Payouts.route)
        assertEquals(MaterialSymbols.FilterList, payoutsAction.icon)
        assertNull(payoutsAction.label)
        assertEquals("Open filter menu", payoutsAction.contentDescription)

        val holdingsAction = BottomBarAction.defaultForRoute(Screen.Holdings.route)
        assertEquals(MaterialSymbols.Add, holdingsAction.icon)
        assertNull(holdingsAction.label)
        assertEquals("Holdings actions", holdingsAction.contentDescription)

        val nullAction = BottomBarAction.defaultForRoute(null)
        assertEquals(MaterialSymbols.Settings, nullAction.icon)
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
            icon = MaterialSymbols.FilterList,
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

    @Test
    fun bottomBarAction_resolvesCloseIconWhenContextualStateIsActive() {
        val action = BottomBarAction(
            icon = MaterialSymbols.FilterList,
            contentDescription = "Open filter menu",
            activeContentDescription = "Close filter menu"
        )

        assertEquals(MaterialSymbols.FilterList, action.resolveIcon(isMenuExpanded = false))
        assertEquals("Open filter menu", action.resolveContentDescription(isMenuExpanded = false))

        assertEquals(MaterialSymbols.Close, action.resolveIcon(isMenuExpanded = true))
        assertEquals("Close filter menu", action.resolveContentDescription(isMenuExpanded = true))

        val activeAction = action.copy(isActive = true)
        assertEquals(MaterialSymbols.Close, activeAction.resolveIcon(isMenuExpanded = false))
        assertEquals("Close filter menu", activeAction.resolveContentDescription(isMenuExpanded = false))
    }

    @Test
    fun bottomBarActionState_registersInvokesAndClearsReselectHandlers() {
        val state = BottomBarActionState()
        var reselectedCount = 0

        state.setReselectHandler(Screen.Overview.route) {
            reselectedCount++
        }

        state.onReselect(Screen.Overview.route)
        assertEquals(1, reselectedCount)

        state.onReselect(Screen.Planner.route)
        assertEquals(1, reselectedCount)

        state.onReselect(null)
        assertEquals(1, reselectedCount)

        state.clearReselectHandler(Screen.Overview.route)
        state.onReselect(Screen.Overview.route)
        assertEquals(1, reselectedCount)
    }
}
