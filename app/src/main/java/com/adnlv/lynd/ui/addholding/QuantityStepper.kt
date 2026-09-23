package com.adnlv.lynd.ui.addholding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun QuantityStepper(
    quantity: String,
    quantityError: String?,
    quantityTextFieldValue: TextFieldValue,
    hasUserTypedQuantity: Boolean,
    onQuantityTextChange: (TextFieldValue) -> Unit,
    onUserTyped: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onUserTyped()
                onDecrement()
            },
            enabled = (quantity.toIntOrNull() ?: 1) > 1
        ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Quantity")
        }

        OutlinedTextField(
            value = quantityTextFieldValue,
            onValueChange = { newValue ->
                onUserTyped()
                onQuantityTextChange(newValue)
            },
            label = { Text("Quantity") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && !hasUserTypedQuantity) {
                        onQuantityTextChange(
                            quantityTextFieldValue.copy(
                                selection = TextRange(0, quantityTextFieldValue.text.length)
                            )
                        )
                    }
                },
            singleLine = true,
            isError = quantityError != null,
            supportingText = quantityError?.let {
                { Text(it) }
            }
        )

        IconButton(
            onClick = {
                onUserTyped()
                onIncrement()
            }
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Quantity")
        }
    }
}
