package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.domain.IsinValidator
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.adnlv.lynd.domain.HoldingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingBottomSheet(
    viewModel: HoldingsViewModel,
    onDismissRequest: () -> Unit,
    holdingToEdit: HoldingItem? = null
) {
    var isSheetExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val focusManager = LocalFocusManager.current

    val prefixes by viewModel.isinPrefixes.collectAsState()
    var selectedPrefix by remember {
        mutableStateOf(holdingToEdit?.isin?.take(6) ?: "UA4000")
    }
    LaunchedEffect(prefixes) {
        if (selectedPrefix.isEmpty() && prefixes.isNotEmpty()) {
            selectedPrefix = prefixes.first()
        }
    }

    var hasUserModifiedIsin by remember { mutableStateOf(false) }
    var prefixDropdownExpanded by remember { mutableStateOf(false) }
    val initialCode = holdingToEdit?.isin?.drop(6) ?: ""
    var codeInput by remember {
        mutableStateOf(TextFieldValue(text = initialCode, selection = TextRange(initialCode.length)))
    }
    var codeError by remember {
        mutableStateOf(IsinValidator.validateCodeInput(initialCode, selectedPrefix))
    }
    var matchingBonds by remember { mutableStateOf<List<String>>(emptyList()) }
    var bondSuggestionsExpanded by remember { mutableStateOf(false) }
    var isCodeFocused by remember { mutableStateOf(false) }

    LaunchedEffect(codeInput.text, selectedPrefix, isCodeFocused) {
        if (isCodeFocused || codeInput.text.isNotEmpty()) {
            val query = "$selectedPrefix${codeInput.text}"
            val results = viewModel.searchBonds(query)
            matchingBonds = results
            bondSuggestionsExpanded = isCodeFocused && results.isNotEmpty()
            codeError = IsinValidator.validateCodeInput(
                code = codeInput.text,
                prefix = selectedPrefix,
                hasMatchingRecord = results.isNotEmpty()
            )
        } else {
            matchingBonds = emptyList()
            bondSuggestionsExpanded = false
            codeError = null
        }
    }

    var quantity by remember { mutableIntStateOf(holdingToEdit?.quantity ?: 1) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var pricePerBondInput by remember {
        mutableStateOf(holdingToEdit?.pricePerBond?.toPlainString() ?: "")
    }
    var totalPriceInput by remember {
        mutableStateOf(holdingToEdit?.totalPaidAmount?.toPlainString() ?: "")
    }

    var purchaseDate by remember {
        mutableStateOf(holdingToEdit?.purchaseDate ?: LocalDate.now())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val animatedSheetRadius by animateDpAsState(
        targetValue = if (isSheetExpanded) 0.dp else 28.dp,
        label = "BottomSheetCornerRadiusAnimation"
    )
    val animatedSheetBorderColor by animateColorAsState(
        targetValue = if (isSheetExpanded) MaterialTheme.colorScheme.surfaceContainerLow else borderColor,
        label = "BottomSheetBorderColorAnimation"
    )

    LaunchedEffect(sheetState) {
        snapshotFlow { sheetState.targetValue }
            .collect { state ->
                when (state) {
                    SheetValue.Expanded -> { isSheetExpanded = true }
                    SheetValue.PartiallyExpanded -> { isSheetExpanded = false }
                    else -> {}
                }
            }
    }

    ModalBottomSheet(
        modifier = Modifier.fillMaxHeight(),
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        contentWindowInsets = { WindowInsets.statusBars },
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = animatedSheetBorderColor,
                    shape = RoundedCornerShape(
                        topStart = animatedSheetRadius,
                        topEnd = animatedSheetRadius,
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable {
                        scope.launch {
                            if (sheetState.currentValue == SheetValue.PartiallyExpanded) {
                                sheetState.expand()
                            } else {
                                sheetState.partialExpand()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                BottomSheetDefaults.DragHandle()
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val fullIsin = "$selectedPrefix${codeInput.text}"
                val isFormValid = fullIsin.length == 12 &&
                    codeError == null &&
                    IsinValidator.isValid(fullIsin) &&
                    quantity >= 1 &&
                    (totalPriceInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } ?: false) &&
                    (pricePerBondInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } ?: false)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (holdingToEdit != null) "Edit Holding" else "Add Holding",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                        onClick = {
                            val perBond = pricePerBondInput.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP) ?: return@Button
                            val totalPaid = totalPriceInput.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP) ?: return@Button

                            val holding = HoldingEntity(
                                id = holdingToEdit?.id ?: 0,
                                isin = fullIsin,
                                quantity = quantity,
                                pricePerBond = perBond,
                                totalPaidAmount = totalPaid,
                                purchaseDate = purchaseDate
                            )
                            if (holdingToEdit != null) {
                                viewModel.updateHolding(holding) {
                                    onDismissRequest()
                                }
                            } else {
                                viewModel.saveHolding(holding) {
                                    onDismissRequest()
                                }
                            }
                        },
                        enabled = isFormValid,
                        border = BorderStroke(1.dp, buttonBorderColor)
                    ) {
                        Text("Save")
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = prefixDropdownExpanded,
                    onExpandedChange = { prefixDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
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
                                    if (selectedPrefix != prefix) {
                                        hasUserModifiedIsin = true
                                    }
                                    selectedPrefix = prefix
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { newValue ->
                            if (newValue.text.length <= 6) {
                                hasUserModifiedIsin = true
                                codeInput = newValue
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
                            .onFocusChanged { isCodeFocused = it.isFocused }
                    )

                    ExposedDropdownMenu(
                        expanded = bondSuggestionsExpanded && matchingBonds.isNotEmpty(),
                        onDismissRequest = { bondSuggestionsExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        matchingBonds.forEach { isin ->
                            DropdownMenuItem(
                                text = { Text(isin) },
                                onClick = {
                                    hasUserModifiedIsin = true
                                    val code = if (isin.startsWith(selectedPrefix)) {
                                        isin.removePrefix(selectedPrefix)
                                    } else {
                                        isin.takeLast(6)
                                    }
                                    codeInput = TextFieldValue(text = code, selection = TextRange(code.length))
                                    codeError = IsinValidator.validateCodeInput(code, selectedPrefix)
                                    bondSuggestionsExpanded = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            onClick = { if (quantity > 1) quantity-- },
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
                            onClick = { quantity++ }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase quantity")
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            text = { Text("Total Paid Price") }
                        )
                        Tab(
                            selected = pagerState.currentPage == 1,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            text = { Text("Price per Bond") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        if (page == 0) {
                            OutlinedTextField(
                                value = totalPriceInput,
                                onValueChange = { input ->
                                    totalPriceInput = input
                                    val parsed = input.toBigDecimalOrNull()
                                    if (parsed != null && parsed >= BigDecimal.ZERO && quantity > 0) {
                                        val computedPerBond = parsed
                                            .divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP)
                                        pricePerBondInput = computedPerBond.toPlainString()
                                    }
                                },
                                label = { Text("Total Paid Price") },
                                placeholder = { Text("1000.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = pricePerBondInput,
                                onValueChange = { input ->
                                    pricePerBondInput = input
                                    val parsed = input.toBigDecimalOrNull()
                                    if (parsed != null && parsed >= BigDecimal.ZERO) {
                                        val computedTotal = parsed
                                            .multiply(BigDecimal(quantity))
                                            .setScale(2, RoundingMode.HALF_UP)
                                        totalPriceInput = computedTotal.toPlainString()
                                    }
                                },
                                label = { Text("Price per Bond") },
                                placeholder = { Text("1000.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Purchase Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select purchase date")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showDatePicker = true }
                )

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = purchaseDate
                            .atStartOfDay(ZoneId.of("UTC"))
                            .toInstant()
                            .toEpochMilli()
                    )

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        purchaseDate = Instant.ofEpochMilli(millis)
                                            .atZone(ZoneId.of("UTC"))
                                            .toLocalDate()
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
        }
    }
}
