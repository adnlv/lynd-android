package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.domain.IsinValidator
import com.adnlv.lynd.ui.holdings.components.AddHoldingHeader
import com.adnlv.lynd.ui.holdings.components.IsinSelectionSection
import com.adnlv.lynd.ui.holdings.components.PriceInputPager
import com.adnlv.lynd.ui.holdings.components.PurchaseDatePickerField
import com.adnlv.lynd.ui.holdings.components.QuantitySelector
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

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

    val prefixes by viewModel.isinPrefixes.collectAsState()
    var selectedPrefix by remember {
        mutableStateOf(holdingToEdit?.isin?.take(6) ?: "UA4000")
    }
    LaunchedEffect(prefixes) {
        if (selectedPrefix.isEmpty() && prefixes.isNotEmpty()) {
            selectedPrefix = prefixes.first()
        }
    }

    val initialCode = holdingToEdit?.isin?.drop(6) ?: ""
    var codeInput by remember {
        mutableStateOf(TextFieldValue(text = initialCode, selection = TextRange(initialCode.length)))
    }
    var codeError by remember {
        mutableStateOf(IsinValidator.validateCodeInput(initialCode, selectedPrefix))
    }
    var matchingBonds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isCodeFocused by remember { mutableStateOf(false) }

    LaunchedEffect(codeInput.text, selectedPrefix, isCodeFocused) {
        if (isCodeFocused || codeInput.text.isNotEmpty()) {
            val query = "$selectedPrefix${codeInput.text}"
            val results = viewModel.searchBonds(query)
            matchingBonds = results
            codeError = IsinValidator.validateCodeInput(
                code = codeInput.text,
                prefix = selectedPrefix,
                hasMatchingRecord = results.isNotEmpty()
            )
        } else {
            matchingBonds = emptyList()
            codeError = null
        }
    }

    var quantity by remember { mutableIntStateOf(holdingToEdit?.quantity ?: 1) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    var pricePerBondInput by remember {
        mutableStateOf(holdingToEdit?.pricePerBond?.toPlainString() ?: "")
    }
    var totalPriceInput by remember {
        mutableStateOf(holdingToEdit?.totalPaidAmount?.toPlainString() ?: "")
    }

    var purchaseDate by remember {
        mutableStateOf(holdingToEdit?.purchaseDate ?: LocalDate.now())
    }

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

                AddHoldingHeader(
                    isEditing = holdingToEdit != null,
                    isFormValid = isFormValid,
                    onSaveClick = {
                        val perBond = pricePerBondInput.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP) ?: return@AddHoldingHeader
                        val totalPaid = totalPriceInput.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP) ?: return@AddHoldingHeader

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
                    }
                )

                IsinSelectionSection(
                    selectedPrefix = selectedPrefix,
                    prefixes = prefixes,
                    onPrefixSelected = { selectedPrefix = it },
                    codeInput = codeInput,
                    onCodeInputChange = { codeInput = it },
                    matchingBonds = matchingBonds,
                    onBondSelected = { isin ->
                        val code = if (isin.startsWith(selectedPrefix)) {
                            isin.removePrefix(selectedPrefix)
                        } else {
                            isin.takeLast(6)
                        }
                        codeInput = TextFieldValue(text = code, selection = TextRange(code.length))
                        codeError = IsinValidator.validateCodeInput(code, selectedPrefix)
                    },
                    codeError = codeError,
                    onFocusChanged = { isCodeFocused = it }
                )

                QuantitySelector(
                    quantity = quantity,
                    onQuantityChange = { quantity = it }
                )

                PriceInputPager(
                    pagerState = pagerState,
                    totalPriceInput = totalPriceInput,
                    onTotalPriceChange = { input ->
                        totalPriceInput = input
                        val parsed = input.toBigDecimalOrNull()
                        if (parsed != null && parsed >= BigDecimal.ZERO && quantity > 0) {
                            val computedPerBond = parsed
                                .divide(BigDecimal(quantity), 2, RoundingMode.HALF_UP)
                            pricePerBondInput = computedPerBond.toPlainString()
                        }
                    },
                    pricePerBondInput = pricePerBondInput,
                    onPricePerBondChange = { input ->
                        pricePerBondInput = input
                        val parsed = input.toBigDecimalOrNull()
                        if (parsed != null && parsed >= BigDecimal.ZERO) {
                            val computedTotal = parsed
                                .multiply(BigDecimal(quantity))
                                .setScale(2, RoundingMode.HALF_UP)
                            totalPriceInput = computedTotal.toPlainString()
                        }
                    }
                )

                PurchaseDatePickerField(
                    purchaseDate = purchaseDate,
                    onDateChange = { purchaseDate = it }
                )
            }
        }
    }
}
