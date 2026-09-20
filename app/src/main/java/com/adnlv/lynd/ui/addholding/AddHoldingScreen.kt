package com.adnlv.lynd.ui.addholding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

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
    var isinNumberTextFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.isinNumber, selection = TextRange(uiState.isinNumber.length)))
    }

    LaunchedEffect(uiState.isinNumber) {
        if (isinNumberTextFieldValue.text != uiState.isinNumber) {
            isinNumberTextFieldValue = TextFieldValue(uiState.isinNumber, selection = TextRange(uiState.isinNumber.length))
        }
    }

    var hasUserTypedQuantity by remember { mutableStateOf(false) }
    var quantityTextFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.quantity))
    }

    LaunchedEffect(uiState.quantity) {
        if (quantityTextFieldValue.text != uiState.quantity) {
            quantityTextFieldValue = quantityTextFieldValue.copy(text = uiState.quantity)
        }
    }

    val pricePagerState = rememberPagerState(
        initialPage = if (uiState.priceMode == PriceInputMode.TOTAL) 1 else 0,
        pageCount = { 2 }
    )

    LaunchedEffect(pricePagerState.currentPage) {
        val targetMode = if (pricePagerState.currentPage == 0) PriceInputMode.PER_BOND else PriceInputMode.TOTAL
        if (uiState.priceMode != targetMode) {
            viewModel.onPriceModeChanged(targetMode)
        }
    }

    LaunchedEffect(uiState.priceMode) {
        val targetPage = if (uiState.priceMode == PriceInputMode.TOTAL) 1 else 0
        if (pricePagerState.currentPage != targetPage) {
            pricePagerState.animateScrollToPage(targetPage)
        }
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val containerFocusRequester = remember { FocusRequester() }

    val heightFraction = 0.85f

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

    val sheetShape = BottomSheetDefaults.ExpandedShape
    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        modifier = modifier,
        dragHandle = null,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Surface(
            shape = sheetShape,
            color = BottomSheetDefaults.ContainerColor,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .imePadding()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(containerFocusRequester)
                        .focusable()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            containerFocusRequester.requestFocus()
                            focusManager.clearFocus(force = true)
                            keyboardController?.hide()
                            viewModel.onDismissDropdown()
                        }
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.editingHoldingId != null) "Edit Bond Holding" else "Add Bond Holding",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val isSaveEnabled = uiState.fetchState is FetchState.Success
                        Button(
                            onClick = viewModel::saveHolding,
                            enabled = isSaveEnabled,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSaveEnabled) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Text("Save")
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                ExposedDropdownMenuBox(
                    expanded = uiState.isPrefixDropdownExpanded,
                    onExpandedChange = viewModel::onPrefixDropdownToggled,
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
                        onDismissRequest = { viewModel.onPrefixDropdownToggled(false) }
                    ) {
                        uiState.availablePrefixes.forEach { prefix ->
                            DropdownMenuItem(
                                text = { Text(prefix) },
                                onClick = { viewModel.onIsinPrefixChanged(prefix) }
                            )
                        }
                    }
                }

                val menuScrollState = rememberScrollState()
                val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                ExposedDropdownMenuBox(
                    expanded = uiState.isDropdownExpanded && uiState.suggestions.isNotEmpty(),
                    onExpandedChange = {
                        viewModel.onIsinFieldTapped()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = isinNumberTextFieldValue,
                        onValueChange = { newValue ->
                            isinNumberTextFieldValue = newValue
                            viewModel.onIsinNumberChanged(newValue.text)
                        },
                        label = { Text("ISIN Number") },
                        placeholder = { Text("018734") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    viewModel.onIsinFieldTapped()
                                }
                            },
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
                        onDismissRequest = viewModel::onDismissDropdown,
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
                                onClick = { viewModel.onIsinSelected(suggestionIsin) }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        hasUserTypedQuantity = true
                        viewModel.decrementQuantity()
                    },
                    enabled = (uiState.quantity.toIntOrNull() ?: 1) > 1
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Quantity")
                }

                OutlinedTextField(
                    value = quantityTextFieldValue,
                    onValueChange = { newValue ->
                        hasUserTypedQuantity = true
                        quantityTextFieldValue = newValue
                        viewModel.onQuantityChanged(newValue.text)
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && !hasUserTypedQuantity) {
                                quantityTextFieldValue = quantityTextFieldValue.copy(
                                    selection = TextRange(0, quantityTextFieldValue.text.length)
                                )
                            }
                        },
                    singleLine = true,
                    isError = uiState.quantityError != null,
                    supportingText = uiState.quantityError?.let {
                        { Text(it) }
                    }
                )

                IconButton(
                    onClick = {
                        hasUserTypedQuantity = true
                        viewModel.incrementQuantity()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Quantity")
                }
            }

            val currency = (uiState.fetchState as? FetchState.Success)?.bond?.currency ?: ""
            val qty = uiState.quantity.toIntOrNull()

            val derivedTotalPriceText = remember(uiState.pricePerBond, qty, currency) {
                if (qty == null || qty <= 0) return@remember null
                val perBond = uiState.pricePerBond.toBigDecimalOrNull() ?: return@remember null
                val total = perBond.multiply(java.math.BigDecimal(qty))
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .toPlainString()
                val prefix = if (currency.isNotBlank()) "$currency " else ""
                "Total: $prefix$total"
            }

            val derivedPerBondPriceText = remember(uiState.totalPrice, qty, currency) {
                if (qty == null || qty <= 0) return@remember null
                val total = uiState.totalPrice.toBigDecimalOrNull() ?: return@remember null
                val perBond = total.divide(java.math.BigDecimal(qty), 2, java.math.RoundingMode.HALF_UP)
                    .toPlainString()
                val prefix = if (currency.isNotBlank()) "$currency " else ""
                "Per bond: $prefix$perBond"
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HorizontalPager(
                    state = pricePagerState,
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (page == 0) {
                                OutlinedTextField(
                                    value = uiState.pricePerBond,
                                    onValueChange = viewModel::onPricePerBondChanged,
                                    label = { Text("Price per Bond") },
                                    placeholder = { Text("e.g. 1025.50") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = uiState.priceError != null && uiState.priceMode == PriceInputMode.PER_BOND,
                                    supportingText = if (uiState.priceMode == PriceInputMode.PER_BOND) {
                                        uiState.priceError?.let { { Text(it) } }
                                    } else null
                                )
                                if (derivedTotalPriceText != null) {
                                    Text(
                                        text = derivedTotalPriceText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = uiState.totalPrice,
                                    onValueChange = viewModel::onTotalPriceChanged,
                                    label = { Text("Total Price") },
                                    placeholder = { Text("e.g. 10255.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    isError = uiState.priceError != null && uiState.priceMode == PriceInputMode.TOTAL,
                                    supportingText = if (uiState.priceMode == PriceInputMode.TOTAL) {
                                        uiState.priceError?.let { { Text(it) } }
                                    } else null
                                )
                                if (derivedPerBondPriceText != null) {
                                    Text(
                                        text = derivedPerBondPriceText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        val isSelected = pricePagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    }
                                )
                        )
                    }
                }
            }

            Text(
                text = "Purchase Date",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.purchaseDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )

            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { millis ->
                    val localDate = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    if (localDate != uiState.purchaseDate) {
                        viewModel.onPurchaseDateChanged(localDate)
                    }
                }
            }

            LaunchedEffect(uiState.purchaseDate) {
                val stateMillis = datePickerState.selectedDateMillis
                val currentLocal = stateMillis?.let {
                    Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                }
                if (currentLocal != uiState.purchaseDate) {
                    datePickerState.selectedDateMillis = uiState.purchaseDate
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli()
                }
            }

            DatePicker(
                state = datePickerState,
                title = null,
                headline = null,
                showModeToggle = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
}
}
}
