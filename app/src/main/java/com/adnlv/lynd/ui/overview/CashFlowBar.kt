package com.adnlv.lynd.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.adnlv.lynd.domain.MonthlyCashFlow
import java.math.BigDecimal

@Composable
fun CashFlowBar(
    monthFlow: MonthlyCashFlow,
    monthLabel: String,
    maxAmount: BigDecimal,
    isSelected: Boolean,
    couponColor: Color,
    principalColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHeightFraction = remember(monthFlow.totalAmount, maxAmount) {
        if (maxAmount > BigDecimal.ZERO && monthFlow.totalAmount > BigDecimal.ZERO) {
            (monthFlow.totalAmount.toFloat() / maxAmount.toFloat()).coerceIn(0.06f, 1f)
        } else {
            0f
        }
    }

    val couponFraction = remember(monthFlow.couponAmount, monthFlow.totalAmount) {
        if (monthFlow.totalAmount > BigDecimal.ZERO) {
            (monthFlow.couponAmount.toFloat() / monthFlow.totalAmount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    val principalFraction = remember(monthFlow.principalAmount, monthFlow.totalAmount) {
        if (monthFlow.totalAmount > BigDecimal.ZERO) {
            (monthFlow.principalAmount.toFloat() / monthFlow.totalAmount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (totalHeightFraction > 0f) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(totalHeightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                ) {
                    if (principalFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(principalFraction)
                                .background(principalColor)
                        )
                    }
                    if (couponFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(couponFraction)
                                .background(couponColor)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 8.dp, height = 2.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = monthLabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
