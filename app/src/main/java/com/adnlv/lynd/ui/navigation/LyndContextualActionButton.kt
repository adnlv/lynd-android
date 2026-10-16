package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
private const val ActionRotationDegrees = 90f

private data class ContextualButtonVisualState(
    val icon: ImageVector,
    val label: String?,
    val contentDescription: String,
    val isContextualActive: Boolean
)

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
    val isContextualActive = action.isContextualActive(menuExpanded)

    val currentVisualState = ContextualButtonVisualState(
        icon = action.resolveIcon(menuExpanded),
        label = action.label,
        contentDescription = action.resolveContentDescription(menuExpanded),
        isContextualActive = isContextualActive
    )

    val rotationTarget = if (isContextualActive) ActionRotationDegrees else 0f
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = ActionTransitionDurationMillis),
        label = "contextual_action_rotation"
    )

    Box(modifier = modifier) {
        LyndBottomBarCircleButton(
            onClick = {
                HapticFeedbackHelper.vibratePageSwitch(view)
                when {
                    menuExpanded -> {
                        menuExpanded = false
                        action.onCancel?.invoke()
                    }
                    action.isActive -> {
                        action.onCancel?.invoke() ?: action.onClick()
                    }
                    action.menuContent != null -> {
                        menuExpanded = true
                    }
                    else -> {
                        action.onClick()
                    }
                }
            }
        ) {
            AnimatedContent(
                targetState = currentVisualState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation },
                transitionSpec = {
                    (fadeIn(
                        animationSpec = tween(durationMillis = ActionTransitionDurationMillis)
                    )).togetherWith(
                        fadeOut(
                            animationSpec = tween(durationMillis = ActionTransitionDurationMillis)
                        )
                    ).using(SizeTransform(clip = false))
                },
                contentAlignment = Alignment.Center,
                contentKey = { targetState -> targetState.icon to targetState.isContextualActive },
                label = "contextual_action_transition"
            ) { targetState ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = targetState.icon,
                        contentDescription = targetState.contentDescription,
                        tint = contentColor,
                        modifier = Modifier.size(ActionIconSize)
                    )
                    if (!targetState.label.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(ActionItemSpacing))
                        Text(
                            text = targetState.label,
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
