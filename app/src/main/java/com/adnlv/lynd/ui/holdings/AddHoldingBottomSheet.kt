package com.adnlv.lynd.ui.holdings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.data.db.HoldingEntity
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHoldingBottomSheet(
    viewModel: HoldingsViewModel,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val prefixes by viewModel.isinPrefixes.collectAsState()

    var selectedPrefix by remember { mutableStateOf("UA4000") }
    LaunchedEffect(prefixes) {
        if (selectedPrefix.isEmpty() && prefixes.isNotEmpty()) {
            selectedPrefix = prefixes.first()
        }
    }

    var prefixDropdownExpanded by remember { mutableStateOf(false) }

    var codeInput by remember { mutableStateOf("") }
    var matchingBonds by remember { mutableStateOf<List<String>>(emptyList()) }
    var bondSuggestionsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(codeInput, selectedPrefix) {
        if (codeInput.isNotEmpty()) {
            val query = "$selectedPrefix$codeInput"
            val results = viewModel.searchBonds(query)
            matchingBonds = results
            bondSuggestionsExpanded = results.isNotEmpty()
        } else {
            matchingBonds = emptyList()
            bondSuggestionsExpanded = false
        }
    }

    var quantity by remember { mutableIntStateOf(1) }

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var pricePerBondInput by remember { mutableStateOf("") }
    var totalPriceInput by remember { mutableStateOf("") }

    var purchaseDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val contentScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return available
            }
        }
    }

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.drawWithContent {
            drawContent()
            val strokeWidth = 1.dp.toPx()
            val halfStroke = strokeWidth / 2f
            val cornerRadius = 28.dp.toPx()

            val borderPath = Path().apply {
                moveTo(halfStroke, size.height)
                lineTo(halfStroke, cornerRadius)
                arcTo(
                    rect = Rect(
                        left = halfStroke,
                        top = halfStroke,
                        right = halfStroke + 2 * (cornerRadius - halfStroke),
                        bottom = halfStroke + 2 * (cornerRadius - halfStroke)
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(size.width - cornerRadius, halfStroke)
                arcTo(
                    rect = Rect(
                        left = size.width - 2 * cornerRadius + halfStroke,
                        top = halfStroke,
                        right = size.width - halfStroke,
                        bottom = halfStroke + 2 * (cornerRadius - halfStroke)
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(size.width - halfStroke, size.height)
            }

            drawPath(
                path = borderPath,
                color = borderColor,
                style = Stroke(width = strokeWidth)
            )
        },
        contentWindowInsets = { WindowInsets.statusBars },
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = "Drag handle"
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.BottomSheetDefaults.DragHandle()
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(contentScrollConnection)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Holding",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

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
                                selectedPrefix = prefix
                                prefixDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = bondSuggestionsExpanded,
                onExpandedChange = { bondSuggestionsExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = {
                        val filtered = it.filter { char -> char.isDigit() }.take(6)
                        codeInput = filtered
                    },
                    label = { Text("Code") },
                    placeholder = { Text("e.g. 238281") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                )

                ExposedDropdownMenu(
                    expanded = bondSuggestionsExpanded,
                    onDismissRequest = { bondSuggestionsExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    matchingBonds.forEach { isin ->
                        DropdownMenuItem(
                            text = { Text(isin) },
                            onClick = {
                                if (isin.startsWith(selectedPrefix)) {
                                    codeInput = isin.removePrefix(selectedPrefix)
                                } else {
                                    codeInput = isin.takeLast(6)
                                }
                                bondSuggestionsExpanded = false
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
                        text = { Text("Price per Bond") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text("Total Paid Price") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    if (page == 0) {
                        OutlinedTextField(
                            value = pricePerBondInput,
                            onValueChange = { input ->
                                pricePerBondInput = input
                                val parsed = input.toDoubleOrNull()
                                if (parsed != null && parsed >= 0) {
                                    val computedTotal = BigDecimal(parsed.toString())
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
                    } else {
                        OutlinedTextField(
                            value = totalPriceInput,
                            onValueChange = { input ->
                                totalPriceInput = input
                                val parsed = input.toDoubleOrNull()
                                if (parsed != null && parsed >= 0 && quantity > 0) {
                                    val computedPerBond = BigDecimal(parsed.toString())
                                        .divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP)
                                    pricePerBondInput = computedPerBond.toPlainString()
                                }
                            },
                            label = { Text("Total Paid Price (inc. fee)") },
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

            val fullIsin = "$selectedPrefix$codeInput"
            val isFormValid = fullIsin.length == 12 &&
                quantity >= 1 &&
                (totalPriceInput.toDoubleOrNull() ?: 0.0) > 0.0 &&
                (pricePerBondInput.toDoubleOrNull() ?: 0.0) > 0.0

            Button(
                onClick = {
                    val perBond = BigDecimal(pricePerBondInput).setScale(2, RoundingMode.HALF_UP)
                    val totalPaid = BigDecimal(totalPriceInput).setScale(2, RoundingMode.HALF_UP)

                    val holding = HoldingEntity(
                        isin = fullIsin,
                        quantity = quantity,
                        pricePerBond = perBond,
                        totalPaidAmount = totalPaid,
                        purchaseDate = purchaseDate
                    )
                    viewModel.saveHolding(holding) {
                        onDismissRequest()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Holding")
            }
        }
    }
}
