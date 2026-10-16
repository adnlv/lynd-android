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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.PayoutBondDetail
import com.adnlv.lynd.domain.PayoutDateGroup
import com.adnlv.lynd.domain.PayoutItem
import com.adnlv.lynd.domain.PayoutLot
import com.adnlv.lynd.domain.PayoutType
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal

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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Prominent Date and Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.formatDate(group.date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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

            Spacer(modifier = Modifier.height(8.dp))

            // Second Row: Total Payout Amount (side-by-side if multiple currencies)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                group.totalAmounts.forEach { total ->
                    val symbol = Formatters.getCurrencySymbol(total.currency)
                    Text(
                        text = "${Formatters.formatAmount(total.amount)} $symbol",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom Area: Case A or Case B
            if (group.isSingleBond) {
                val bond = group.singleBond!!
                val symbol = Formatters.getCurrencySymbol(bond.currency)
                val hasRedemption = bond.payoutTypes.contains(PayoutType.REDEMPTION)
                val hasCoupon = bond.payoutTypes.contains(PayoutType.COUPON)

                val breakdown = when {
                    hasRedemption && hasCoupon -> " • Principal: ${Formatters.formatAmount(bond.principalAmount)} $symbol, Coupon: ${Formatters.formatAmount(bond.couponAmount)} $symbol"
                    hasRedemption -> " • Principal: ${Formatters.formatAmount(bond.principalAmount)} $symbol"
                    else -> ""
                }

                Text(
                    text = "${bond.isin} • ${bond.totalQuantity} bonds$breakdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
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

@Composable
fun PayoutCard(
    payout: PayoutItem,
    modifier: Modifier = Modifier,
    tab: PayoutTab = PayoutTab.UPCOMING
) {
    val symbol = remember(payout.currency) { Formatters.getCurrencySymbol(payout.currency) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = payout.isin,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${Formatters.formatAmount(payout.payoutAmount)} $symbol",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
