package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit = {},
    val menuContent: (@Composable ColumnScope.(onDismissRequest: () -> Unit) -> Unit)? = null
) {
    companion object {
        fun defaultForRoute(route: String?): BottomBarAction = when (route) {
            Screen.Overview.route -> BottomBarAction(
                icon = Icons.Default.Tune,
                contentDescription = "Overview actions"
            )
            Screen.Planner.route -> BottomBarAction(
                icon = Icons.Default.FilterList,
                contentDescription = "Planner actions"
            )
            Screen.Payouts.route -> BottomBarAction(
                icon = Icons.Default.FilterList,
                contentDescription = "Open filter menu"
            )
            Screen.Holdings.route -> BottomBarAction(
                icon = Icons.Default.Add,
                contentDescription = "Holdings actions"
            )
            else -> BottomBarAction(
                icon = Icons.Default.Tune,
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
