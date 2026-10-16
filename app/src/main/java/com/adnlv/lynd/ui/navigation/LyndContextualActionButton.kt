package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adnlv.lynd.util.HapticFeedbackHelper

private val ActionIconSize = 20.dp
private val ActionItemSpacing = 4.dp
private const val ActionTransitionDurationMillis = 220

@Composable
fun LyndContextualActionButton(
    action: BottomBarAction,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var menuExpanded by remember(action.icon, action.label, action.contentDescription) {
        mutableStateOf(false)
    }
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

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
            AnimatedContent(
                targetState = action,
                modifier = Modifier.fillMaxSize(),
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = tween(durationMillis = ActionTransitionDurationMillis),
                        initialOffsetY = { fullHeight -> fullHeight }
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = ActionTransitionDurationMillis)
                    )).togetherWith(
                        slideOutVertically(
                            animationSpec = tween(durationMillis = ActionTransitionDurationMillis),
                            targetOffsetY = { fullHeight -> fullHeight }
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = ActionTransitionDurationMillis)
                        )
                    ).using(SizeTransform(clip = false))
                },
                contentAlignment = Alignment.Center,
                contentKey = { targetAction -> targetAction.icon },
                label = "contextual_action_transition"
            ) { targetAction ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = targetAction.icon,
                        contentDescription = targetAction.contentDescription,
                        tint = contentColor,
                        modifier = Modifier.size(ActionIconSize)
                    )
                    if (!targetAction.label.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(ActionItemSpacing))
                        Text(
                            text = targetAction.label,
                            color = contentColor,
                            style = TextStyle(
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.Normal,
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
            }
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
