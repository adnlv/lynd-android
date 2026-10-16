package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.adnlv.lynd.ui.icons.Add
import com.adnlv.lynd.ui.icons.FilterList
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.icons.Settings

data class BottomBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val label: String? = null,
    val onClick: () -> Unit = {},
    val menuContent: (@Composable ColumnScope.(onDismissRequest: () -> Unit) -> Unit)? = null
) {
    companion object {
        fun defaultForRoute(route: String?): BottomBarAction = when (route) {
            Screen.Overview.route -> BottomBarAction(
                icon = MaterialSymbols.Settings,
                contentDescription = "Overview actions"
            )
            Screen.Planner.route -> BottomBarAction(
                icon = MaterialSymbols.FilterList,
                contentDescription = "Planner actions"
            )
            Screen.Payouts.route -> BottomBarAction(
                icon = MaterialSymbols.FilterList,
                contentDescription = "Open filter menu"
            )
            Screen.Holdings.route -> BottomBarAction(
                icon = MaterialSymbols.Add,
                contentDescription = "Holdings actions"
            )
            else -> BottomBarAction(
                icon = MaterialSymbols.Settings,
                contentDescription = "Contextual actions"
            )
        }
    }
}

@Stable
class BottomBarActionState {
    private val actionsByRoute = mutableStateMapOf<String, BottomBarAction>()

    fun setAction(route: String, action: BottomBarAction) {
        actionsByRoute[route] = action
    }

    fun clearAction(route: String) {
        actionsByRoute.remove(route)
    }

    fun actionForRoute(route: String?): BottomBarAction {
        if (route == null) return BottomBarAction.defaultForRoute(null)
        return actionsByRoute[route] ?: BottomBarAction.defaultForRoute(route)
    }
}

@Composable
fun rememberBottomBarActionState(): BottomBarActionState {
    return remember { BottomBarActionState() }
}
