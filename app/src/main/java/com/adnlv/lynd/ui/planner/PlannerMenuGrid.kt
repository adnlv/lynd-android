package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class PlannerMenuItem(
    val tab: PlannerTab,
    val title: String,
    val icon: ImageVector
)

@Composable
fun PlannerMenuCard(
    item: PlannerMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = modifier.height(12.dp))
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview
@Composable
private fun PlannerMenuCardPreview() {
    PlannerMenuCard(
        item = PlannerMenuItem(
            tab = PlannerTab.INCOME_GAPS,
            title = "Income Gaps",
            icon = Icons.Default.DateRange
        ),
        onClick = {}
    )
}

@Composable
fun PlannerMenuGrid(
    onTabSelected: (PlannerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        PlannerMenuItem(
            tab = PlannerTab.INCOME_GAPS,
            title = "Income Gaps",
            icon = Icons.Outlined.DateRange
        ),
        PlannerMenuItem(
            tab = PlannerTab.COMPOUNDING,
            title = "Compounding",
            icon = Icons.AutoMirrored.Outlined.TrendingUp
        ),
        PlannerMenuItem(
            tab = PlannerTab.PURCHASING_POWER,
            title = "Purchasing Power",
            icon = Icons.Outlined.ShoppingCart
        ),
        PlannerMenuItem(
            tab = PlannerTab.MATURITY_REBALANCING,
            title = "Maturity Alerts",
            icon = Icons.Outlined.NotificationsActive
        )
    )

    val gap = 8.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            space = gap,
            alignment = Alignment.Top
        )
    ) {
        val rows = items.chunked(2)

        for (rowItems in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                for (item in rowItems) {
                    PlannerMenuCard(
                        item = item,
                        onClick = { onTabSelected(item.tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlannerMenuGridPreview() {
    PlannerMenuGrid(onTabSelected = {})
}
