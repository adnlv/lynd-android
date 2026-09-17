package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingItem

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingsScreen(
    viewModel: HoldingsViewModel,
    modifier: Modifier = Modifier,
    onNavigateToAdd: (() -> Unit)? = null,
    onNavigateToEdit: ((HoldingItem) -> Unit)? = null,
    addHoldingContent: (@Composable (sheetState: SheetState, holdingToEdit: HoldingItem?, onDismiss: () -> Unit) -> Unit)? = null
) {
    val holdings by viewModel.holdings.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var showHoldingSheet by rememberSaveable { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingItem?>(null) }
    var revealedHoldingId by remember { mutableStateOf<Int?>(null) }
    var swipingHoldingId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            revealedHoldingId = null
            swipingHoldingId = null
        }
    }

    val isSheetActive = showHoldingSheet && sheetState.targetValue != SheetValue.Hidden
    val blurRadius by animateDpAsState(
        targetValue = if (isSheetActive) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "holdingsBackgroundBlur"
    )

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.weight(1f)
                ) { data ->
                    val totalSeconds = 4
                    var remainingSeconds by remember(data) { mutableStateOf(totalSeconds) }
                    val progress = remember(data) { Animatable(1f) }
                    val swipeOffsetX = remember(data) { Animatable(0f) }

                    LaunchedEffect(data) {
                        launch {
                            while (remainingSeconds > 0) {
                                kotlinx.coroutines.delay(1000L)
                                remainingSeconds -= 1
                            }
                        }
                        progress.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = totalSeconds * 1000, easing = androidx.compose.animation.core.LinearEasing)
                        )
                    }

                    val dismissThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .offset { IntOffset(swipeOffsetX.value.roundToInt(), 0) }
                            .alpha(1f - (kotlin.math.abs(swipeOffsetX.value) / (dismissThresholdPx * 2f)).coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(16.dp))
                            .pointerInput(data) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { _, dragAmount ->
                                        coroutineScope.launch {
                                            swipeOffsetX.snapTo(swipeOffsetX.value + dragAmount)
                                        }
                                    },
                                    onDragEnd = {
                                        coroutineScope.launch {
                                            if (kotlin.math.abs(swipeOffsetX.value) > dismissThresholdPx) {
                                                val target = if (swipeOffsetX.value > 0) dismissThresholdPx * 3 else -dismissThresholdPx * 3
                                                swipeOffsetX.animateTo(target)
                                                data.dismiss()
                                            } else {
                                                swipeOffsetX.animateTo(0f)
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            swipeOffsetX.animateTo(0f)
                                        }
                                    }
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shadowElevation = 6.dp
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 16.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = data.visuals.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${remainingSeconds}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }

                                data.visuals.actionLabel?.let { label ->
                                    TextButton(
                                        onClick = { data.performAction() },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            LinearProgressIndicator(
                                progress = { progress.value },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        revealedHoldingId = null
                        if (addHoldingContent != null) {
                            editingHolding = null
                            showHoldingSheet = true
                        } else {
                            onNavigateToAdd?.invoke()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Holding")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier
                )
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

            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No holdings yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = holdings, key = { it.id }) { holding ->
                        HoldingCard(
                            modifier = Modifier.animateItem(),
                            holding = holding,
                            isRevealed = revealedHoldingId == holding.id,
                            canSwipe = swipingHoldingId == null || swipingHoldingId == holding.id,
                            onExpand = { revealedHoldingId = holding.id },
                            onCollapse = {
                                if (revealedHoldingId == holding.id) {
                                    revealedHoldingId = null
                                }
                            },
                            onDragStart = {
                                if (swipingHoldingId == null) {
                                    swipingHoldingId = holding.id
                                    if (revealedHoldingId != holding.id) {
                                        revealedHoldingId = null
                                    }
                                }
                            },
                            onDragEnd = {
                                if (swipingHoldingId == holding.id) {
                                    swipingHoldingId = null
                                }
                            },
                            onDragCancel = {
                                if (swipingHoldingId == holding.id) {
                                    swipingHoldingId = null
                                }
                            },
                            onEdit = {
                                revealedHoldingId = null
                                if (addHoldingContent != null) {
                                    editingHolding = holding
                                    showHoldingSheet = true
                                } else {
                                    onNavigateToEdit?.invoke(holding)
                                }
                            },
                            onDelete = {
                                revealedHoldingId = null
                                viewModel.deleteHolding(holding.id)
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Holding deleted",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
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
    }

    if (showHoldingSheet && addHoldingContent != null) {
        addHoldingContent(sheetState, editingHolding) {
            showHoldingSheet = false
            editingHolding = null
        }
    }
}

@Composable
fun HoldingCard(
    holding: HoldingItem,
    isRevealed: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    canSwipe: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val actionButtonsWidthDp = 152.dp
    val actionButtonsWidthPx = with(density) { actionButtonsWidthDp.toPx() }
    val offsetX = remember { Animatable(0f) }
    val slideAwayOffsetX = remember { Animatable(0f) }
    var itemWidthPx by remember { mutableFloatStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }

    val swipeSpringSpec = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMediumLow
    )

    LaunchedEffect(isRevealed, isDeleting) {
        if (isDeleting) return@LaunchedEffect
        val target = if (isRevealed) -actionButtonsWidthPx else 0f
        if (offsetX.value != target) {
            offsetX.animateTo(target, animationSpec = swipeSpringSpec)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemWidthPx = it.width.toFloat() }
            .offset { IntOffset(slideAwayOffsetX.value.roundToInt(), 0) }
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.width(76.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Holding",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(
                onClick = {
                    if (!isDeleting) {
                        isDeleting = true
                        coroutineScope.launch {
                            val targetOffset = if (itemWidthPx > 0f) -itemWidthPx - with(density) { 32.dp.toPx() } else -1500f
                            slideAwayOffsetX.animateTo(
                                targetValue = targetOffset,
                                animationSpec = tween(durationMillis = 250, easing = FastOutLinearInEasing)
                            )
                            onDelete()
                        }
                    }
                },
                modifier = Modifier.width(76.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Holding",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        val maxOverdragPx = with(density) { 40.dp.toPx() }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(actionButtonsWidthPx, isDeleting, canSwipe) {
                    if (isDeleting) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (canSwipe) {
                                onDragStart?.invoke()
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!canSwipe) return@detectHorizontalDragGestures
                            coroutineScope.launch {
                                val current = offsetX.value
                                val effectiveDelta = if (dragAmount < 0f && current <= -actionButtonsWidthPx) {
                                    val currentOverdrag = (-actionButtonsWidthPx - current).coerceAtLeast(0f)
                                    val progress = (currentOverdrag / maxOverdragPx).coerceIn(0f, 1f)
                                    dragAmount * (1f - progress) * 0.5f
                                } else if (dragAmount > 0f && current >= 0f) {
                                    val currentOverdrag = current.coerceAtLeast(0f)
                                    val progress = (currentOverdrag / maxOverdragPx).coerceIn(0f, 1f)
                                    dragAmount * (1f - progress) * 0.5f
                                } else {
                                    dragAmount
                                }
                                val target = (current + effectiveDelta).coerceIn(
                                    -actionButtonsWidthPx - maxOverdragPx,
                                    maxOverdragPx
                                )
                                offsetX.snapTo(target)
                            }
                        },
                        onDragEnd = {
                            onDragEnd?.invoke()
                            coroutineScope.launch {
                                if (offsetX.value < -actionButtonsWidthPx / 3) {
                                    onExpand()
                                    offsetX.animateTo(-actionButtonsWidthPx, animationSpec = swipeSpringSpec)
                                } else {
                                    onCollapse()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                }
                            }
                        },
                        onDragCancel = {
                            onDragCancel?.invoke()
                            coroutineScope.launch {
                                if (offsetX.value < -actionButtonsWidthPx / 3) {
                                    onExpand()
                                    offsetX.animateTo(-actionButtonsWidthPx, animationSpec = swipeSpringSpec)
                                } else {
                                    onCollapse()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                }
                            }
                        }
                    )
                }
                .then(
                    if (isRevealed) {
                        Modifier.clickable {
                            onCollapse()
                        }
                    } else {
                        Modifier
                    }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = holding.bondName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ISIN: ${holding.isin}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Quantity: ${holding.quantity} | Price: ${holding.pricePerBond} | Total: ${holding.totalPaidAmount}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Purchased: ${holding.purchaseDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
