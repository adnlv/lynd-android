package com.adnlv.lynd.ui.holdings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adnlv.lynd.data.db.HoldingEntity
import com.adnlv.lynd.domain.BondPriceCalculator
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.domain.IsinValidator
import com.adnlv.lynd.ui.holdings.components.AddHoldingHeader
import com.adnlv.lynd.ui.holdings.components.BondPriceInputs
import com.adnlv.lynd.ui.holdings.components.IsinSelectionSection
import com.adnlv.lynd.ui.holdings.components.PurchaseDatePickerField
import com.adnlv.lynd.ui.holdings.components.QuantitySelector
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

@Composable
fun AddHoldingDialog(
    viewModel: HoldingsViewModel,
    onDismissRequest: () -> Unit,
    holdingToEdit: HoldingItem? = null
) {
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

    var pricePerBondInput by remember {
        mutableStateOf(holdingToEdit?.pricePerBond?.toPlainString() ?: "")
    }
    var totalPriceInput by remember {
        mutableStateOf(holdingToEdit?.totalPaidAmount?.toPlainString() ?: "")
    }

    var purchaseDate by remember {
        mutableStateOf(holdingToEdit?.purchaseDate ?: LocalDate.now())
    }

    val fullIsin = "$selectedPrefix${codeInput.text}"
    val isFormValid = fullIsin.length == 12 &&
        codeError == null &&
        IsinValidator.isValid(fullIsin) &&
        quantity >= 1 &&
        (totalPriceInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } ?: false) &&
        (pricePerBondInput.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } ?: false)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentWindowInsets = WindowInsets.navigationBars,
            topBar = {
                AddHoldingHeader(
                    isEditing = holdingToEdit != null,
                    isFormValid = isFormValid,
                    onCloseClick = onDismissRequest,
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
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: ISIN Data Fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                    }
                }

                // Card 2: Quantity & Price Data Fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuantitySelector(
                            quantity = quantity,
                            onQuantityChange = { newQuantity ->
                                quantity = newQuantity
                                val parsedPricePerBond = pricePerBondInput.toBigDecimalOrNull()
                                if (parsedPricePerBond != null && parsedPricePerBond >= BigDecimal.ZERO && newQuantity > 0) {
                                    val computedTotal = BondPriceCalculator.calculateTotalPrice(parsedPricePerBond, newQuantity)
                                    totalPriceInput = computedTotal.toPlainString()
                                }
                            }
                        )

                        HorizontalDivider()

                        BondPriceInputs(
                            totalPriceInput = totalPriceInput,
                            onTotalPriceChange = { input ->
                                totalPriceInput = input
                                val parsed = input.toBigDecimalOrNull()
                                if (parsed != null && parsed >= BigDecimal.ZERO && quantity > 0) {
                                    val computedPerBond = BondPriceCalculator.calculatePricePerBond(parsed, quantity)
                                    pricePerBondInput = computedPerBond.toPlainString()
                                }
                            },
                            pricePerBondInput = pricePerBondInput,
                            onPricePerBondChange = { input ->
                                pricePerBondInput = input
                                val parsed = input.toBigDecimalOrNull()
                                if (parsed != null && parsed >= BigDecimal.ZERO && quantity > 0) {
                                    val computedTotal = BondPriceCalculator.calculateTotalPrice(parsed, quantity)
                                    totalPriceInput = computedTotal.toPlainString()
                                }
                            },
                            quantity = quantity
                        )
                    }
                }

                // Card 3: Date Data Field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        PurchaseDatePickerField(
                            purchaseDate = purchaseDate,
                            onDateChange = { purchaseDate = it }
                        )
                    }
                }
            }
        }
    }
}
