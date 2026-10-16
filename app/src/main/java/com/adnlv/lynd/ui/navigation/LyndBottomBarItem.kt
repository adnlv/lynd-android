package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adnlv.lynd.ui.icons.Event
import com.adnlv.lynd.ui.icons.Home
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.icons.PlannerReview

private val IconSize = 20.dp
private val ItemSpacing = 4.dp

fun Screen.icon(): ImageVector = when (this) {
    Screen.Overview -> MaterialSymbols.Home
    Screen.Planner -> MaterialSymbols.PlannerReview
    Screen.Payouts -> MaterialSymbols.Event
    Screen.Holdings -> Icons.AutoMirrored.Filled.List
}

@Composable
fun LyndBottomBarItem(
    screen: Screen,
    selected: Boolean,
    onPressStart: () -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnPressStart by rememberUpdatedState(onPressStart)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnCancel by rememberUpdatedState(onCancel)

    val itemColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "tab_color"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .semantics {
                this.selected = selected
                this.role = Role.Tab
                onClick {
                    currentOnRelease()
                    true
                }
            }
            .pointerInput(screen) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    currentOnPressStart()
                    val up = waitForUpOrCancellation()
                    if (up != null &&
                        up.position.x in 0f..size.width.toFloat() &&
                        up.position.y in 0f..size.height.toFloat()
                    ) {
                        currentOnRelease()
                    } else {
                        currentOnCancel()
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon(),
            contentDescription = screen.title,
            tint = itemColor,
            modifier = Modifier.size(IconSize)
        )
        Spacer(modifier = Modifier.height(ItemSpacing))
        Text(
            text = screen.title,
            color = itemColor,
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
