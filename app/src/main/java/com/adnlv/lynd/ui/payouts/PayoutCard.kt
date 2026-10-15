package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.adnlv.lynd.util.Formatters

@Composable
fun PayoutCard(
    payout: PayoutItem,
    tab: PayoutTab,
    modifier: Modifier = Modifier
) {
    val (cardAlpha, accentColor) = when (tab) {
        PayoutTab.UPCOMING -> 1.0f to MaterialTheme.colorScheme.primary
        PayoutTab.RECEIVED -> 0.75f to MaterialTheme.colorScheme.primary
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
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = payout.payType,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            val formattedAmount = remember(payout.payoutAmount) {
                Formatters.formatPayoutAmount(payout.payoutAmount)
            }
            Text(
                text = "$formattedAmount ${Formatters.getCurrencySymbol(payout.currency)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

