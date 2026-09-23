package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.PayoutItem
import java.math.RoundingMode

@Composable
fun PayoutCard(
    payout: PayoutItem,
    tab: PayoutTab,
    modifier: Modifier = Modifier
) {
    val (cardAlpha, accentColor, iconBgColor, iconTintColor) = when (tab) {
        PayoutTab.UPCOMING -> Quadruple(
            1.0f,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        PayoutTab.RECEIVED -> Quadruple(
            0.75f,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        PayoutTab.HISTORICAL -> Quadruple(
            0.50f,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isRedemption = payout.payType.equals("Redemption", ignoreCase = true)
            val icon = if (isRedemption) {
                Icons.Default.DoneAll
            } else {
                Icons.Default.Percent
            }
            val iconShape = if (isRedemption) {
                CircleShape
            } else {
                remember { Cookie9Shape() }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(42.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val iconBgSize = if (isRedemption) 40.dp else 42.dp
                    Box(
                        modifier = Modifier
                            .size(iconBgSize)
                            .background(color = iconBgColor, shape = iconShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = payout.payType,
                            tint = iconTintColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = payout.payType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            val formattedAmount = remember(payout.payoutAmount) {
                payout.payoutAmount.setScale(2, RoundingMode.HALF_UP).toPlainString()
            }
            Text(
                text = "$formattedAmount ${payout.currency}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
