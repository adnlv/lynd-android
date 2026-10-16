package com.adnlv.lynd.ui.payouts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.PayoutType

@Composable
fun PayoutTypeBadge(
    type: PayoutType,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor, text) = when (type) {
        PayoutType.COUPON -> Triple(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Coupon"
        )
        PayoutType.REDEMPTION -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Redemption"
        )
    }

    Badge(
        containerColor = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
