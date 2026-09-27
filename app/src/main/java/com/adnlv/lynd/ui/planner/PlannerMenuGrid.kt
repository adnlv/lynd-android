package com.adnlv.lynd.ui.planner

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class PlannerMenuItem(
    val tab: PlannerTab,
    val title: String,
    val icon: ImageVector
)

@Composable
fun PlannerMenuButton(
    item: PlannerMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PlannerMenuGrid(
    onTabSelected: (PlannerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        PlannerMenuItem(PlannerTab.INCOME_GAPS, "Income Gaps", Icons.Default.DateRange),
        PlannerMenuItem(PlannerTab.COMPOUNDING, "Compounding", Icons.Default.TrendingUp),
        PlannerMenuItem(PlannerTab.PURCHASING_POWER, "Purchasing Power", Icons.Default.PriceChange),
        PlannerMenuItem(PlannerTab.MATURITY_REBALANCING, "Maturity Alerts", Icons.Default.NotificationsActive)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlannerMenuButton(
                item = items[0],
                onClick = { onTabSelected(items[0].tab) },
                modifier = Modifier.weight(1f)
            )
            PlannerMenuButton(
                item = items[1],
                onClick = { onTabSelected(items[1].tab) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlannerMenuButton(
                item = items[2],
                onClick = { onTabSelected(items[2].tab) },
                modifier = Modifier.weight(1f)
            )
            PlannerMenuButton(
                item = items[3],
                onClick = { onTabSelected(items[3].tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
