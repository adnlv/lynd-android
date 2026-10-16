package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.PayoutType
import com.adnlv.lynd.ui.icons.ConfirmationNumber
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.icons.ModeHeat

private data class BadgeConfig(
    val containerColor: Color,
    val contentColor: Color,
    val icon: ImageVector,
    val contentDescription: String
)

@Composable
fun PayoutTypeBadge(
    type: PayoutType,
    modifier: Modifier = Modifier
) {
    val config = when (type) {
        PayoutType.COUPON -> BadgeConfig(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = MaterialSymbols.ConfirmationNumber,
            contentDescription = "Coupon"
        )
        PayoutType.REDEMPTION -> BadgeConfig(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            icon = MaterialSymbols.ModeHeat,
            contentDescription = "Redemption"
        )
    }

    Badge(
        containerColor = config.containerColor,
        contentColor = config.contentColor,
        modifier = modifier.clip(RoundedCornerShape(6.dp))
    ) {
        Icon(
            imageVector = config.icon,
            contentDescription = config.contentDescription,
            modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .size(14.dp)
        )
    }
}
