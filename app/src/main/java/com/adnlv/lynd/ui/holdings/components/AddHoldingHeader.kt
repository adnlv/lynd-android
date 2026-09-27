package com.adnlv.lynd.ui.holdings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun AddHoldingHeader(
    isEditing: Boolean,
    isFormValid: Boolean,
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
            Text(
                text = if (isEditing) "Edit Holding" else "Add Holding",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        val buttonColors = ButtonDefaults.buttonColors()
        val primaryColor = MaterialTheme.colorScheme.primary
        val activeBorderColor = primaryColor.copy(
            red = primaryColor.red * 0.8f,
            green = primaryColor.green * 0.8f,
            blue = primaryColor.blue * 0.8f
        )
        val buttonBorderColor = if (isFormValid) {
            activeBorderColor
        } else {
            buttonColors.disabledContainerColor
        }

        Button(
            onClick = onSaveClick,
            enabled = isFormValid,
            border = BorderStroke(1.dp, buttonBorderColor)
        ) {
            Text("Save")
        }
    }
}
