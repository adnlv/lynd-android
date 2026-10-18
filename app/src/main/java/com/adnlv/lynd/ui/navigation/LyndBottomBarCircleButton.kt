package com.adnlv.lynd.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.adnlv.lynd.ui.components.LyndGlassSurface
import com.adnlv.lynd.ui.theme.LyndLayoutDefaults

@Composable
fun LyndBottomBarCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    LyndGlassSurface(
        shape = CircleShape,
        modifier = modifier.size(LyndLayoutDefaults.FloatingBarHeight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
