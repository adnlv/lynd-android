package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingGroup
import com.adnlv.lynd.domain.HoldingItem

@Composable
fun HoldingGroupCard(
    group: HoldingGroup,
    revealedHoldingId: Int?,
    swipingHoldingId: Int?,
    peekingHoldingId: Int?,
    onExpandHolding: (Int) -> Unit,
    onCollapseHolding: (Int) -> Unit,
    onDragStartHolding: (Int) -> Unit,
    onDragEndHolding: (Int) -> Unit,
    onDragCancelHolding: (Int) -> Unit,
    onEditHolding: (HoldingItem) -> Unit,
    onDeleteHolding: (HoldingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.isin,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
                ) {
                    Text(
                        text = "${group.totalQuantity}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (group.currency.isNotBlank()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                    ) {
                        Text(
                            text = group.currency,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            group.items.forEachIndexed { index, holding ->
                val itemShape = when {
                    group.items.size == 1 -> RoundedCornerShape(16.dp)
                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    index == group.items.lastIndex -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    else -> RoundedCornerShape(4.dp)
                }
                key(holding.id) {
                    HoldingCard(
                        holding = holding,
                        isRevealed = revealedHoldingId == holding.id,
                        isPeeking = peekingHoldingId == holding.id,
                        canSwipe = swipingHoldingId == null || swipingHoldingId == holding.id,
                        onExpand = { onExpandHolding(holding.id) },
                        onCollapse = { onCollapseHolding(holding.id) },
                        onDragStart = { onDragStartHolding(holding.id) },
                        onDragEnd = { onDragEndHolding(holding.id) },
                        onDragCancel = { onDragCancelHolding(holding.id) },
                        onEdit = { onEditHolding(holding) },
                        onDelete = { onDeleteHolding(holding) },
                        shape = itemShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
