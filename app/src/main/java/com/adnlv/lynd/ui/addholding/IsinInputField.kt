package com.adnlv.lynd.ui.addholding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsinInputField(
    uiState: AddHoldingUiState,
    isinNumberTextFieldValue: TextFieldValue,
    onIsinNumberChange: (TextFieldValue) -> Unit,
    onPrefixDropdownToggled: (Boolean) -> Unit,
    onIsinPrefixChanged: (String) -> Unit,
    onIsinFieldTapped: () -> Unit,
    onDismissDropdown: () -> Unit,
    onIsinSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        ExposedDropdownMenuBox(
            expanded = uiState.isPrefixDropdownExpanded,
            onExpandedChange = onPrefixDropdownToggled,
            modifier = Modifier.width(112.dp)
        ) {
            OutlinedTextField(
                value = uiState.isinPrefix,
                onValueChange = {},
                readOnly = true,
                label = { Text("Prefix") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isPrefixDropdownExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = uiState.isPrefixDropdownExpanded,
                onDismissRequest = { onPrefixDropdownToggled(false) }
            ) {
                uiState.availablePrefixes.forEach { prefix ->
                    DropdownMenuItem(
                        text = { Text(prefix) },
                        onClick = { onIsinPrefixChanged(prefix) }
                    )
                }
            }
        }

        val menuScrollState = rememberScrollState()
        val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

        ExposedDropdownMenuBox(
            expanded = uiState.isDropdownExpanded && uiState.suggestions.isNotEmpty(),
            onExpandedChange = {
                onIsinFieldTapped()
            },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = isinNumberTextFieldValue,
                onValueChange = onIsinNumberChange,
                label = { Text("ISIN Number") },
                placeholder = { Text("018734") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.fetchState is FetchState.Error,
                supportingText = (uiState.fetchState as? FetchState.Error)?.message?.let {
                    { Text(it) }
                },
                trailingIcon = {
                    if (uiState.fetchState is FetchState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (uiState.fetchState is FetchState.Success) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Bond found",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else if (uiState.fetchState is FetchState.Idle && uiState.isinNumber.length in 0..5) {
                        Text(
                            text = "${6 - uiState.isinNumber.length}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = uiState.isDropdownExpanded && uiState.suggestions.isNotEmpty(),
                onDismissRequest = onDismissDropdown,
                scrollState = menuScrollState,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .heightIn(max = 144.dp)
                    .drawWithContent {
                        drawContent()
                        val totalScroll = menuScrollState.maxValue
                        if (totalScroll > 0) {
                            val verticalPadding = 4.dp.toPx()
                            val viewHeight = size.height - (verticalPadding * 2)
                            val contentHeight = viewHeight + totalScroll
                            val thumbHeight = (viewHeight * (viewHeight / contentHeight)).coerceAtLeast(16.dp.toPx())
                            val scrollProgress = menuScrollState.value.toFloat() / totalScroll.toFloat()
                            val thumbOffsetY = verticalPadding + (scrollProgress * (viewHeight - thumbHeight))
                            val barWidth = 3.dp.toPx()
                            val rightMargin = 2.dp.toPx()

                            drawRoundRect(
                                color = scrollbarColor,
                                topLeft = Offset(size.width - barWidth - rightMargin, thumbOffsetY),
                                size = Size(barWidth, thumbHeight),
                                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                            )
                        }
                    }
            ) {
                uiState.suggestions.forEach { suggestionIsin ->
                    val suffix = suggestionIsin.removePrefix(uiState.isinPrefix)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = suffix,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        onClick = { onIsinSelected(suggestionIsin) }
                    )
                }
            }
        }
    }
}
