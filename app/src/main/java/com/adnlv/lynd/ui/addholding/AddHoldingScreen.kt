package com.adnlv.lynd.ui.addholding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.ui.components.CompactMonthDatePicker
import java.time.YearMonth

private const val BASE_YEAR = 2000
private const val TOTAL_CALENDAR_MONTHS = 2400

private fun pageFromYearMonth(ym: YearMonth): Int =
    (ym.year - BASE_YEAR) * 12 + (ym.monthValue - 1)

@OptIn(ExperimentalMaterial3Api::class)
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

    val calendarPagerInitialPage = remember(uiState.purchaseDate) {
        pageFromYearMonth(YearMonth.from(uiState.purchaseDate))
    }
    val calendarPagerState = rememberPagerState(
        initialPage = calendarPagerInitialPage,
        pageCount = { TOTAL_CALENDAR_MONTHS }
    )

    val contentScrollState = rememberScrollState()

    val contentNestedScrollConnection = remember(calendarPagerState, pricePagerState, contentScrollState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (calendarPagerState.isScrollInProgress || pricePagerState.isScrollInProgress) {
                    return Offset(x = 0f, y = available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (calendarPagerState.isScrollInProgress || pricePagerState.isScrollInProgress) {
                    return Offset(x = 0f, y = available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (calendarPagerState.isScrollInProgress || pricePagerState.isScrollInProgress) {
                    return Velocity(x = 0f, y = available.y)
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                if (calendarPagerState.isScrollInProgress || pricePagerState.isScrollInProgress) {
                    return Velocity(x = 0f, y = available.y)
                }
                return Velocity.Zero
            }
        }
    }

    val sheetShape = BottomSheetDefaults.ExpandedShape
    ModalBottomSheet(
        onDismissRequest = onNavigateBack,
        sheetState = sheetState,
        shape = sheetShape,
        containerColor = BottomSheetDefaults.ContainerColor,
        tonalElevation = 0.dp,
        modifier = modifier,
        dragHandle = null,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        Surface(
            shape = sheetShape,
            color = BottomSheetDefaults.ContainerColor,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction)
                .drawWithContent {
                    drawContent()
                    val strokeWidth = 1.dp.toPx()
                    val halfStroke = strokeWidth / 2f
                    val cornerRadiusPx = 28.dp.toPx()
                    drawLine(
                        color = outlineColor,
                        start = Offset(halfStroke, cornerRadiusPx),
                        end = Offset(halfStroke, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(size.width - halfStroke, cornerRadiusPx),
                        end = Offset(size.width - halfStroke, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = outlineColor,
                        start = Offset(cornerRadiusPx, halfStroke),
                        end = Offset(size.width - cornerRadiusPx, halfStroke),
                        strokeWidth = strokeWidth
                    )
                    drawArc(
                        color = outlineColor,
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = Size(cornerRadiusPx * 2 - strokeWidth, cornerRadiusPx * 2 - strokeWidth),
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = outlineColor,
                        startAngle = 270f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(size.width - cornerRadiusPx * 2 + halfStroke, halfStroke),
                        size = Size(cornerRadiusPx * 2 - strokeWidth, cornerRadiusPx * 2 - strokeWidth),
                        style = Stroke(width = strokeWidth)
                    )
                }
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                .semantics { contentDescription = "Drag handle" }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 14.dp),
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
                            .nestedScroll(contentNestedScrollConnection)
                            .verticalScroll(contentScrollState)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IsinInputField(
                            uiState = uiState,
                            isinNumberTextFieldValue = isinNumberTextFieldValue,
                            onIsinNumberChange = { newValue ->
                                isinNumberTextFieldValue = newValue
                                viewModel.onIsinNumberChanged(newValue.text)
                            },
                            onPrefixDropdownToggled = viewModel::onPrefixDropdownToggled,
                            onIsinPrefixChanged = viewModel::onIsinPrefixChanged,
                            onIsinFieldTapped = viewModel::onIsinFieldTapped,
                            onDismissDropdown = viewModel::onDismissDropdown,
                            onIsinSelected = viewModel::onIsinSelected
                        )

                        QuantityStepper(
                            quantity = uiState.quantity,
                            quantityError = uiState.quantityError,
                            quantityTextFieldValue = quantityTextFieldValue,
                            hasUserTypedQuantity = hasUserTypedQuantity,
                            onQuantityTextChange = { newValue ->
                                quantityTextFieldValue = newValue
                                viewModel.onQuantityChanged(newValue.text)
                            },
                            onUserTyped = { hasUserTypedQuantity = true },
                            onIncrement = viewModel::incrementQuantity,
                            onDecrement = viewModel::decrementQuantity
                        )

                        PriceInputPager(
                            pricePagerState = pricePagerState,
                            uiState = uiState,
                            onPricePerBondChanged = viewModel::onPricePerBondChanged,
                            onTotalPriceChanged = viewModel::onTotalPriceChanged
                        )

                        Text(
                            text = "Purchase Date",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        CompactMonthDatePicker(
                            selectedDate = uiState.purchaseDate,
                            onDateSelected = viewModel::onPurchaseDateChanged,
                            pagerState = calendarPagerState,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
