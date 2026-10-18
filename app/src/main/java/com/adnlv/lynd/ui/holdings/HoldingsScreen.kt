package com.adnlv.lynd.ui.holdings

import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.ui.icons.Add
import com.adnlv.lynd.ui.icons.MaterialSymbols
import com.adnlv.lynd.ui.navigation.BottomBarAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    viewModel: HoldingsViewModel,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onActionChange: ((BottomBarAction) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToAdd: (() -> Unit)? = null,
    onNavigateToEdit: ((HoldingItem) -> Unit)? = null
) {
    val holdings by viewModel.holdings.collectAsState()
    val groupedHoldings by viewModel.groupedHoldings.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    var revealedHoldingId by remember { mutableStateOf<Int?>(null) }
    var swipingHoldingId by remember { mutableStateOf<Int?>(null) }
    var peekingHoldingId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("lynd_prefs", Context.MODE_PRIVATE)
    }
    var hasSeenSwipePeek by remember {
        mutableStateOf(sharedPrefs.getBoolean("has_seen_swipe_peek", false))
    }

    SideEffect {
        val isHoldingContextualActive = showAddDialog || editingHolding != null || revealedHoldingId != null
        onActionChange?.invoke(
            BottomBarAction(
                icon = MaterialSymbols.Add,
                contentDescription = "Add Holding",
                isActive = isHoldingContextualActive,
                activeContentDescription = "Cancel holding action",
                onClick = {
                    revealedHoldingId = null
                    if (onNavigateToAdd != null) {
                        onNavigateToAdd.invoke()
                    } else {
                        showAddDialog = true
                    }
                },
                onCancel = {
                    showAddDialog = false
                    editingHolding = null
                    revealedHoldingId = null
                }
            )
        )
    }

    LaunchedEffect(groupedHoldings, hasSeenSwipePeek) {
        if (!hasSeenSwipePeek && groupedHoldings.isNotEmpty()) {
            val firstHolding = groupedHoldings.first().items.firstOrNull() ?: return@LaunchedEffect
            delay(500)
            peekingHoldingId = firstHolding.id
            delay(1400)
            peekingHoldingId = null
            sharedPrefs.edit().putBoolean("has_seen_swipe_peek", true).apply()
            hasSeenSwipePeek = true
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            revealedHoldingId = null
            swipingHoldingId = null
            if (peekingHoldingId != null) {
                peekingHoldingId = null
                sharedPrefs.edit().putBoolean("has_seen_swipe_peek", true).apply()
                hasSeenSwipePeek = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) { data ->
                HoldingsUndoSnackbar(data = data)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (revealedHoldingId != null) {
                            revealedHoldingId = null
                        }
                    }
                }
        ) {
            if (onNavigateBack != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Payouts"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payouts",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            HoldingsSyncBanner(
                syncError = syncError,
                onRetry = { viewModel.retrySync() }
            )

            if (holdings.isEmpty()) {
                Text(
                    text = "Holdings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                com.adnlv.lynd.ui.components.LyndEmptyState(
                    title = "No holdings yet",
                    description = "Add your first bond holding to start tracking your investment portfolio.",
                    primaryActionLabel = "Add Holding",
                    onPrimaryAction = {
                        if (onNavigateToAdd != null) {
                            onNavigateToAdd.invoke()
                        } else {
                            showAddDialog = true
                        }
                    },
                    onLoadTestPortfolio = { viewModel.loadTestPortfolio() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = groupedHoldings, key = { it.isin }) { group ->
                        HoldingGroupCard(
                            modifier = Modifier.animateItem(),
                            group = group,
                            revealedHoldingId = revealedHoldingId,
                            swipingHoldingId = swipingHoldingId,
                            peekingHoldingId = peekingHoldingId,
                            onExpandHolding = { revealedHoldingId = it },
                            onCollapseHolding = {
                                if (revealedHoldingId == it) {
                                    revealedHoldingId = null
                                }
                            },
                            onDragStartHolding = { id ->
                                if (peekingHoldingId != null) {
                                    peekingHoldingId = null
                                    sharedPrefs.edit().putBoolean("has_seen_swipe_peek", true).apply()
                                    hasSeenSwipePeek = true
                                }
                                if (swipingHoldingId == null) {
                                    swipingHoldingId = id
                                    if (revealedHoldingId != id) {
                                        revealedHoldingId = null
                                    }
                                }
                            },
                            onDragEndHolding = { id ->
                                if (swipingHoldingId == id) {
                                    swipingHoldingId = null
                                }
                            },
                            onDragCancelHolding = { id ->
                                if (swipingHoldingId == id) {
                                    swipingHoldingId = null
                                }
                            },
                            onEditHolding = { holding ->
                                revealedHoldingId = null
                                if (onNavigateToEdit != null) {
                                    onNavigateToEdit.invoke(holding)
                                } else {
                                    editingHolding = holding
                                }
                            },
                            onDeleteHolding = { holding ->
                                revealedHoldingId = null
                                viewModel.deleteHolding(holding.id)
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Holding deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Indefinite
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreHolding()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog || editingHolding != null) {
            AddHoldingDialog(
                viewModel = viewModel,
                onDismissRequest = {
                    showAddDialog = false
                    editingHolding = null
                },
                holdingToEdit = editingHolding
            )
        }
    }
}
