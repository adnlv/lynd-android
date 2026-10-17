package com.adnlv.lynd.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.adnlv.lynd.ui.components.LyndGlassSurface
import com.adnlv.lynd.ui.theme.LyndGlassDefaults
import com.adnlv.lynd.util.HapticFeedbackHelper

private val ActionIconSize = 20.dp
private val ActionItemSpacing = 4.dp
private val PopupCornerRadius = 24.dp
private val PopupVerticalSpacing = 8.dp
private val PopupContentVerticalPadding = 12.dp
private const val ActionTransitionDurationMillis = 220
private const val PopupAnimationDurationMillis = 180
private const val PopupClosedScale = 0.85f

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
                val displayIcon = targetAction.resolveIcon(menuExpanded)
                val displayContentDescription = targetAction.resolveContentDescription(menuExpanded)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = displayIcon,
                        contentDescription = displayContentDescription,
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
            LyndContextualPopupMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                menuContent { menuExpanded = false }
            }
        }
    }
}

@Composable
private fun LyndContextualPopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedState = remember { MutableTransitionState(false) }
    expandedState.targetState = expanded

    if (expandedState.currentState || expandedState.targetState) {
        val density = LocalDensity.current
        val positionProvider = remember(density) {
            ContextualMenuPositionProvider(density)
        }
        val transition = rememberTransition(expandedState, label = "contextual_popup_transition")
        val scale by transition.animateFloat(
            transitionSpec = { tween(durationMillis = PopupAnimationDurationMillis) },
            label = "popup_scale"
        ) { isExpanded ->
            if (isExpanded) 1f else PopupClosedScale
        }
        val alpha by transition.animateFloat(
            transitionSpec = { tween(durationMillis = PopupAnimationDurationMillis) },
            label = "popup_alpha"
        ) { isExpanded ->
            if (isExpanded) 1f else 0f
        }

        Popup(
            popupPositionProvider = positionProvider,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .padding(LyndGlassDefaults.ShadowElevation)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        transformOrigin = TransformOrigin(1f, 1f)
                    }
            ) {
                LyndGlassSurface(
                    shape = RoundedCornerShape(PopupCornerRadius),
                    modifier = Modifier.width(IntrinsicSize.Max)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = PopupContentVerticalPadding)
                            .verticalScroll(rememberScrollState()),
                        content = content
                    )
                }
            }
        }
    }
}

private class ContextualMenuPositionProvider(
    private val density: Density
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val shadowPaddingPx = with(density) { LyndGlassDefaults.ShadowElevation.roundToPx() }
        val verticalSpacingPx = with(density) { PopupVerticalSpacing.roundToPx() }

        val targetX = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.right - popupContentSize.width + shadowPaddingPx
        } else {
            anchorBounds.left - shadowPaddingPx
        }
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = targetX.coerceIn(0, maxX)

        val targetY = anchorBounds.top - popupContentSize.height + shadowPaddingPx - verticalSpacingPx
        val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
        val y = targetY.coerceIn(0, maxY)

        return IntOffset(x, y)
    }
}
