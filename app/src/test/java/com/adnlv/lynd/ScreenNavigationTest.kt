package com.adnlv.lynd

import com.adnlv.lynd.ui.navigation.Screen
import org.junit.Assert.assertEquals
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
}
