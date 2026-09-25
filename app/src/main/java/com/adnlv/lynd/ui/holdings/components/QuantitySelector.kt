package com.adnlv.lynd.ui.holdings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                enabled = quantity > 1
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease quantity")
            }

            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = { onQuantityChange(quantity + 1) }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
            }
        }
    }
}
