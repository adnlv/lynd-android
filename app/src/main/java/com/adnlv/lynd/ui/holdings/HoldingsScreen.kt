package com.adnlv.lynd.ui.holdings

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    viewModel: HoldingsViewModel,
    modifier: Modifier = Modifier,
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
    var showAddSheet by remember { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val sharedPrefs = remember(context) {
        context.getSharedPreferences("lynd_prefs", Context.MODE_PRIVATE)
    }
    var hasSeenSwipePeek by remember {
        mutableStateOf(sharedPrefs.getBoolean("has_seen_swipe_peek", false))
    }

    LaunchedEffect(holdings, hasSeenSwipePeek) {
        if (!hasSeenSwipePeek && holdings.isNotEmpty()) {
            val firstHolding = holdings.first()
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
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 0.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        revealedHoldingId = null
                        if (onNavigateToAdd != null) {
                            onNavigateToAdd.invoke()
                        } else {
                            showAddSheet = true
                        }
                    },
                    modifier = Modifier
                        .padding(end = 4.dp, top = 4.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            shape = FloatingActionButtonDefaults.shape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Holding"
                    )
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.fillMaxWidth()
                ) { data ->
                    HoldingsUndoSnackbar(data = data)
                }
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
            if (isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            HoldingsSyncBanner(
                syncError = syncError,
                onRetry = { viewModel.retrySync() }
            )

            if (holdings.isEmpty()) {
                HoldingsEmptyState(
                    onLoadTestPortfolio = { viewModel.loadTestPortfolio() }
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 88.dp
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

        if (showAddSheet || editingHolding != null) {
            AddHoldingBottomSheet(
                viewModel = viewModel,
                onDismissRequest = {
                    showAddSheet = false
                    editingHolding = null
                },
                holdingToEdit = editingHolding
            )
        }
    }
}
