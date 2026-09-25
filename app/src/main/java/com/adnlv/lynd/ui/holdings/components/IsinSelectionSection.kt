package com.adnlv.lynd.ui.holdings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsinSelectionSection(
    selectedPrefix: String,
    prefixes: List<String>,
    onPrefixSelected: (String) -> Unit,
    codeInput: TextFieldValue,
    onCodeInputChange: (TextFieldValue) -> Unit,
    matchingBonds: List<String>,
    onBondSelected: (String) -> Unit,
    codeError: String?,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var prefixDropdownExpanded by remember { mutableStateOf(false) }
    var bondSuggestionsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            ExposedDropdownMenuBox(
                expanded = prefixDropdownExpanded,
                onExpandedChange = { prefixDropdownExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = selectedPrefix,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("ISIN Prefix") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prefixDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = prefixDropdownExpanded,
                    onDismissRequest = { prefixDropdownExpanded = false }
                ) {
                    prefixes.forEach { prefix ->
                        DropdownMenuItem(
                            text = { Text(prefix) },
                            onClick = {
                                onPrefixSelected(prefix)
                                prefixDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = bondSuggestionsExpanded && matchingBonds.isNotEmpty(),
                onExpandedChange = { expanded ->
                    bondSuggestionsExpanded = expanded && matchingBonds.isNotEmpty()
                },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { newValue ->
                        if (newValue.text.length <= 6) {
                            onCodeInputChange(newValue)
                        }
                    },
                    label = { Text("Code") },
                    placeholder = { Text("238281") },
                    singleLine = true,
                    isError = codeError != null,
                    supportingText = {
                        Text(
                            text = "${codeInput.text.length}/6",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .onFocusChanged {
                            onFocusChanged(it.isFocused)
                            if (it.isFocused && matchingBonds.isNotEmpty()) {
                                bondSuggestionsExpanded = true
                            }
                        }
                )

                ExposedDropdownMenu(
                    expanded = bondSuggestionsExpanded && matchingBonds.isNotEmpty(),
                    onDismissRequest = { bondSuggestionsExpanded = false }
                ) {
                    matchingBonds.forEach { isin ->
                        DropdownMenuItem(
                            text = { Text(isin) },
                            onClick = {
                                onBondSelected(isin)
                                bondSuggestionsExpanded = false
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }
        }

        if (codeError != null) {
            Text(
                text = codeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
