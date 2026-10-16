package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

val FloatingBarHeight = 64.dp

@Composable
fun LyndBottomBarCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val navBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val barBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)

    Box(
        modifier = modifier
            .size(FloatingBarHeight)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
            .clip(CircleShape)
            .background(barBackgroundColor)
            .border(
                width = 0.5.dp,
                color = navBorderColor,
                shape = CircleShape
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
