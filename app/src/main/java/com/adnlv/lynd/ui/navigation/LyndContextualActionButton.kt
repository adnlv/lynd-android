package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.util.HapticFeedbackHelper

private val ActionIconSize = 20.dp

@Composable
fun LyndContextualActionButton(
    action: BottomBarAction,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var menuExpanded by remember(action.icon, action.contentDescription) { mutableStateOf(false) }

    Box(modifier = modifier) {
        LyndBottomBarCircleButton(
            onClick = {
                HapticFeedbackHelper.vibratePageSwitch(view)
                if (action.menuContent != null) {
                    menuExpanded = true
                } else {
                    action.onClick()
                }
            }
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ActionIconSize)
            )
        }

        val menuContent = action.menuContent
        if (menuContent != null) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                menuContent { menuExpanded = false }
            }
        }
    }
}
