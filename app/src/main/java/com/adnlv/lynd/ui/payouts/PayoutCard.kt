package com.adnlv.lynd.ui.payouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adnlv.lynd.domain.PayoutDateGroup
import com.adnlv.lynd.domain.PayoutLot
import com.adnlv.lynd.domain.PayoutType
import com.adnlv.lynd.ui.icons.Event
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.theme.RobotoFlexHeavy
import com.adnlv.lynd.util.Formatters

@Composable
fun PayoutCard(
    group: PayoutDateGroup,
    modifier: Modifier = Modifier
) {
    var isExpandedSingle by rememberSaveable { mutableStateOf(false) }
    var expandedIsin by rememberSaveable { mutableStateOf<String?>(null) }

    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .animateContentSize()
            .then(
                if (group.isSingleBond) {
                    Modifier.clickable { isExpandedSingle = !isExpandedSingle }
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Prominent Date with Event Icon and Summary Badges
            val dateColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Event,
                        contentDescription = null,
                        tint = dateColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = Formatters.formatDate(group.date),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = dateColor
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (group.payoutTypes.contains(PayoutType.COUPON)) {
                        PayoutTypeBadge(type = PayoutType.COUPON)
                    }
                    if (group.payoutTypes.contains(PayoutType.REDEMPTION)) {
                        PayoutTypeBadge(type = PayoutType.REDEMPTION)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val dotTrailColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                group.totalAmounts.forEach { total ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val symbol = Formatters.getCurrencySymbol(total.currency)
                        Text(
                            text = "${Formatters.formatAmount(total.amount)} $symbol",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = RobotoFlexHeavy,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 5.dp)
                                .height(1.dp)
                                .drawBehind {
                                    val strokeWidth = 1.5.dp.toPx()
                                    val dotInterval = 5.dp.toPx()
                                    drawLine(
                                        color = dotTrailColor,
                                        start = Offset(0f, size.height / 2),
                                        end = Offset(size.width, size.height / 2),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(0.1f, dotInterval),
                                            0f
                                        )
                                    )
                                }
                        )
                        Text(
                            text = total.currency.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            color = dateColor
                        )
                    }
                }
            }

            if (group.isSingleBond) {
                val bond = group.singleBond!!

                AnimatedVisibility(
                    visible = isExpandedSingle,
                    enter = expandVertically(
                        animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(durationMillis = 120, delayMillis = 60)),
                    exit = shrinkVertically(
                        animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                    ) + fadeOut(animationSpec = tween(durationMillis = 120))
                ) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        LotBreakdownList(
                            lots = bond.lots,
                            currency = bond.currency
                        )
                    }
                }
            } else {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.bonds.forEach { bond ->
                        val isBondExpanded = expandedIsin == bond.isin
                        val symbol = Formatters.getCurrencySymbol(bond.currency)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    expandedIsin = if (isBondExpanded) null else bond.isin
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bond.isin} (${bond.totalQuantity})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    if (bond.payoutTypes.contains(PayoutType.COUPON)) {
                                        PayoutTypeBadge(type = PayoutType.COUPON)
                                    }
                                    if (bond.payoutTypes.contains(PayoutType.REDEMPTION)) {
                                        PayoutTypeBadge(type = PayoutType.REDEMPTION)
                                    }
                                }

                                Text(
                                    text = "${Formatters.formatAmount(bond.totalAmount)} $symbol",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            AnimatedVisibility(
                                visible = isBondExpanded,
                                enter = expandVertically(
                                    animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                                ) + fadeIn(animationSpec = tween(durationMillis = 120, delayMillis = 60)),
                                exit = shrinkVertically(
                                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                                ) + fadeOut(animationSpec = tween(durationMillis = 120))
                            ) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    LotBreakdownList(
                                        lots = bond.lots,
                                        currency = bond.currency
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LotBreakdownList(
    lots: List<PayoutLot>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val symbol = remember(currency) { Formatters.getCurrencySymbol(currency) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Purchase Lots",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        lots.forEach { lot ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Formatters.formatDate(lot.purchaseDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = "${lot.quantity} bonds",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${Formatters.formatAmount(lot.amount)} $symbol",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
