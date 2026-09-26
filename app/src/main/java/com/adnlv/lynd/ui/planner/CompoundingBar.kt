package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adnlv.lynd.domain.CompoundingPoint
import java.math.BigDecimal

@Composable
fun CompoundingBar(
    point: CompoundingPoint,
    maxAmount: BigDecimal,
    isSelected: Boolean,
    withdrawColor: Color,
    reinvestColor: Color,
    extraColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val withdrawHeightFraction = remember(point.withdrawProfit, maxAmount) {
        if (maxAmount > BigDecimal.ZERO && point.withdrawProfit > BigDecimal.ZERO) {
            (point.withdrawProfit.toFloat() / maxAmount.toFloat()).coerceIn(0.06f, 1f)
        } else {
            0.06f
        }
    }

    val reinvestHeightFraction = remember(point.reinvestProfit, maxAmount) {
        if (maxAmount > BigDecimal.ZERO && point.reinvestProfit > BigDecimal.ZERO) {
            (point.reinvestProfit.toFloat() / maxAmount.toFloat()).coerceIn(0.06f, 1f)
        } else {
            0.06f
        }
    }

    val extraFraction = remember(point.extraProfit, point.reinvestProfit) {
        if (point.reinvestProfit > BigDecimal.ZERO) {
            (point.extraProfit.toFloat() / point.reinvestProfit.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    val baseWithdrawFraction = 1f - extraFraction

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .height(130.dp)
                .width(44.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Left bar: Withdrawn payouts
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(withdrawHeightFraction)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(withdrawColor.copy(alpha = if (isSelected) 1f else 0.8f))
            )

            // Right bar: Reinvested payouts (with extra compound profit on top)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(reinvestHeightFraction)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            ) {
                if (extraFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(extraFraction.coerceAtLeast(0.01f))
                            .background(extraColor.copy(alpha = if (isSelected) 1f else 0.85f))
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(baseWithdrawFraction.coerceAtLeast(0.01f))
                        .background(reinvestColor.copy(alpha = if (isSelected) 1f else 0.7f))
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Y${point.year}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                )
        )
    }
}
