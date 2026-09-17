package com.adnlv.lynd.ui.addholding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import com.adnlv.lynd.domain.HoldingItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHoldingScreen(
    viewModel: AddHoldingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialHolding: HoldingItem? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    val isKeyboardOpen = WindowInsets.isImeVisible
    val heightFraction = if (isKeyboardOpen) 1.0f else 0.80f

    LaunchedEffect(initialHolding) {
        if (initialHolding != null) {
            viewModel.initializeForEdit(
                holdingId = initialHolding.id,
                isin = initialHolding.isin,
                quantity = initialHolding.quantity,
                pricePerBond = initialHolding.pricePerBond,
                purchaseDate = initialHolding.purchaseDate
            )
        }
    }

    LaunchedEffect(viewModel.saveSuccessEvent) {
        viewModel.saveSuccessEvent.collect {
            sheetState.hide()
            onNavigateBack()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = null,
        scrimColor = Color.Black.copy(alpha = 0.28f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.editingHoldingId != null) "Edit Bond Holding" else "Add Bond Holding",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = viewModel::saveHolding,
                    enabled = uiState.fetchState is FetchState.Success
                ) {
                    Text("Save")
                }
            }
            OutlinedTextField(
                value = uiState.isin,
                onValueChange = viewModel::onIsinChanged,
                label = { Text("ISIN Code") },
                placeholder = { Text("e.g. UA4000187348") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                    } else if (uiState.fetchState is FetchState.Idle && uiState.isin.trim().length in 1..11) {
                        Text(
                            text = "${12 - uiState.isin.trim().length}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            if (uiState.fetchState is FetchState.Success) {
                val bond = (uiState.fetchState as FetchState.Success).bond
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = bond.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Currency: ${bond.currency} | Coupon: ${bond.couponRate}% | Matures: ${bond.maturityDate}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = viewModel::decrementQuantity,
                    enabled = (uiState.quantity.toIntOrNull() ?: 1) > 1
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Quantity")
                }

                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = viewModel::onQuantityChanged,
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = uiState.quantityError != null,
                    supportingText = uiState.quantityError?.let {
                        { Text(it) }
                    }
                )

                IconButton(
                    onClick = viewModel::incrementQuantity
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Quantity")
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.priceMode == PriceInputMode.PER_BOND,
                    onClick = { viewModel.onPriceModeChanged(PriceInputMode.PER_BOND) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Per Bond")
                }
                SegmentedButton(
                    selected = uiState.priceMode == PriceInputMode.TOTAL,
                    onClick = { viewModel.onPriceModeChanged(PriceInputMode.TOTAL) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Total")
                }
            }

            when (uiState.priceMode) {
                PriceInputMode.PER_BOND -> {
                    OutlinedTextField(
                        value = uiState.pricePerBond,
                        onValueChange = viewModel::onPricePerBondChanged,
                        label = { Text("Price per Bond") },
                        placeholder = { Text("e.g. 1025.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.priceError != null,
                        supportingText = uiState.priceError?.let {
                            { Text(it) }
                        }
                    )
                }
                PriceInputMode.TOTAL -> {
                    OutlinedTextField(
                        value = uiState.totalPrice,
                        onValueChange = viewModel::onTotalPriceChanged,
                        label = { Text("Total Price") },
                        placeholder = { Text("e.g. 10255.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.priceError != null,
                        supportingText = uiState.priceError?.let {
                            { Text(it) }
                        }
                    )
                }
            }

            val currency = (uiState.fetchState as? FetchState.Success)?.bond?.currency ?: ""
            val derivedPriceText = remember(uiState.priceMode, uiState.pricePerBond, uiState.totalPrice, uiState.quantity, currency) {
                val qty = uiState.quantity.toIntOrNull()
                if (qty == null || qty <= 0) return@remember null

                when (uiState.priceMode) {
                    PriceInputMode.PER_BOND -> {
                        val perBond = uiState.pricePerBond.toBigDecimalOrNull() ?: return@remember null
                        val total = perBond.multiply(java.math.BigDecimal(qty))
                            .setScale(2, java.math.RoundingMode.HALF_UP)
                            .toPlainString()
                        val prefix = if (currency.isNotBlank()) "$currency " else ""
                        "Total: $prefix$total"
                    }
                    PriceInputMode.TOTAL -> {
                        val total = uiState.totalPrice.toBigDecimalOrNull() ?: return@remember null
                        val perBond = total.divide(java.math.BigDecimal(qty), 2, java.math.RoundingMode.HALF_UP)
                            .toPlainString()
                        val prefix = if (currency.isNotBlank()) "$currency " else ""
                        "Per bond: $prefix$perBond"
                    }
                }
            }

            if (derivedPriceText != null) {
                Text(
                    text = derivedPriceText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = uiState.purchaseDate.toString(),
                onValueChange = {},
                label = { Text("Purchase Date") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.purchaseDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            viewModel.onPurchaseDateChanged(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
