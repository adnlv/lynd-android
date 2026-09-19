package com.adnlv.lynd.ui.holdings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adnlv.lynd.domain.HoldingGroup
import com.adnlv.lynd.domain.HoldingItem
import com.adnlv.lynd.util.Formatters
import java.math.BigDecimal

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import com.adnlv.lynd.util.HapticFeedbackHelper
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
    val groupedHoldings by viewModel.groupedHoldings.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    var showHoldingSheet by rememberSaveable { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingItem?>(null) }
    var revealedHoldingId by remember { mutableStateOf<Int?>(null) }
    var swipingHoldingId by remember { mutableStateOf<Int?>(null) }
    var peekingHoldingId by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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

    var isFabExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            revealedHoldingId = null
            swipingHoldingId = null
            if (peekingHoldingId != null) {
                peekingHoldingId = null
                sharedPrefs.edit().putBoolean("has_seen_swipe_peek", true).apply()
                hasSeenSwipePeek = true
            }
            delay(500L)
            if (listState.isScrollInProgress) {
                isFabExpanded = false
            }
        } else {
            delay(1000L)
            isFabExpanded = true
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 0.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        revealedHoldingId = null
                        if (addHoldingContent != null) {
                            editingHolding = null
                            showHoldingSheet = true
                        } else {
                            onNavigateToAdd?.invoke()
                        }
                    },
                    modifier = Modifier.padding(end = 4.dp, top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = if (isFabExpanded) 20.dp else 16.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Holding"
                        )
                        AnimatedVisibility(
                            visible = isFabExpanded,
                            enter = androidx.compose.animation.fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = 100,
                                    easing = FastOutSlowInEasing
                                )
                            ) + androidx.compose.animation.expandHorizontally(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            ),
                            exit = androidx.compose.animation.fadeOut(
                                animationSpec = tween(
                                    durationMillis = 200,
                                    easing = FastOutSlowInEasing
                                )
                            ) + androidx.compose.animation.shrinkHorizontally(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Add holding",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.fillMaxWidth()
                ) { data ->
                    val totalSeconds = 3
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
                            animationSpec = tween(
                                durationMillis = totalSeconds * 1000,
                                easing = androidx.compose.animation.core.LinearEasing
                            )
                        )
                        data.dismiss()
                    }

                    val dismissThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
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
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
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
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${remainingSeconds}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
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

            AnimatedVisibility(
                visible = syncError != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                syncError?.let { error ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Sync Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sync error",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                    maxLines = 2
                                )
                            }
                            TextButton(
                                onClick = { viewModel.retrySync() },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "Retry",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            if (holdings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "No holdings yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { viewModel.loadTestPortfolio() }
                        ) {
                            Text("Load test portfolio")
                        }
                    }
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
                                if (addHoldingContent != null) {
                                    editingHolding = holding
                                    showHoldingSheet = true
                                } else {
                                    onNavigateToEdit?.invoke(holding)
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
    }

    if (showHoldingSheet && addHoldingContent != null) {
        addHoldingContent(sheetState, editingHolding) {
            showHoldingSheet = false
            editingHolding = null
        }
    }
}

@Composable
fun HoldingGroupCard(
    group: HoldingGroup,
    revealedHoldingId: Int?,
    swipingHoldingId: Int?,
    peekingHoldingId: Int?,
    onExpandHolding: (Int) -> Unit,
    onCollapseHolding: (Int) -> Unit,
    onDragStartHolding: (Int) -> Unit,
    onDragEndHolding: (Int) -> Unit,
    onDragCancelHolding: (Int) -> Unit,
    onEditHolding: (HoldingItem) -> Unit,
    onDeleteHolding: (HoldingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.isin,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Badge(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = "${group.totalQuantity}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            group.items.forEachIndexed { index, holding ->
                val itemShape = when {
                    group.items.size == 1 -> RoundedCornerShape(16.dp)
                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    index == group.items.lastIndex -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    else -> RoundedCornerShape(4.dp)
                }
                key(holding.id) {
                    HoldingCard(
                        holding = holding,
                        isRevealed = revealedHoldingId == holding.id,
                        isPeeking = peekingHoldingId == holding.id,
                        canSwipe = swipingHoldingId == null || swipingHoldingId == holding.id,
                        onExpand = { onExpandHolding(holding.id) },
                        onCollapse = { onCollapseHolding(holding.id) },
                        onDragStart = { onDragStartHolding(holding.id) },
                        onDragEnd = { onDragEndHolding(holding.id) },
                        onDragCancel = { onDragCancelHolding(holding.id) },
                        onEdit = { onEditHolding(holding) },
                        onDelete = { onDeleteHolding(holding) },
                        shape = itemShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun HoldingCard(
    holding: HoldingItem,
    isRevealed: Boolean,
    isPeeking: Boolean = false,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDragStart: (() -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    onDragCancel: (() -> Unit)? = null,
    canSwipe: Boolean = true,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    shape: Shape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val actionButtonsWidthDp = 144.dp
    val actionButtonsWidthPx = with(density) { actionButtonsWidthDp.toPx() }
    val baseButtonWidthDp = 60.dp
    val offsetX = remember { Animatable(0f) }
    val slideAwayOffsetX = remember { Animatable(0f) }
    var itemWidthPx by remember { mutableFloatStateOf(0f) }
    var isDeleting by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var hasTriggeredRevealHaptic by remember { mutableStateOf(false) }

    val swipeSpringSpec = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMediumLow
    )

    LaunchedEffect(isPeeking) {
        if (isDeleting || isRevealed) return@LaunchedEffect
        if (isPeeking) {
            val peekDistance = -actionButtonsWidthPx * 0.6f
            offsetX.animateTo(
                targetValue = peekDistance,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        } else if (offsetX.value != 0f) {
            offsetX.animateTo(
                targetValue = 0f,
                animationSpec = swipeSpringSpec
            )
        }
    }

    LaunchedEffect(isRevealed, isDeleting) {
        if (isDeleting) return@LaunchedEffect
        val target = if (isRevealed) -actionButtonsWidthPx else 0f
        if (offsetX.value != target) {
            offsetX.animateTo(target, animationSpec = swipeSpringSpec)
        }
    }

    val dragDistance = (-offsetX.value).coerceAtLeast(0f)
    val fullSwipeThresholdPx = if (itemWidthPx > 0f) {
        (itemWidthPx * 0.45f).coerceAtLeast(actionButtonsWidthPx * 1.5f)
    } else {
        with(density) { 220.dp.toPx() }
    }
    val maxDragLeftPx = if (itemWidthPx > 0f) itemWidthPx else with(density) { 360.dp.toPx() }

    val (deleteWidthDp, editWidthDp, deleteAlpha) = run {
        if (dragDistance <= actionButtonsWidthPx) {
            val progress = (dragDistance / actionButtonsWidthPx).coerceIn(0f, 1f)
            Triple(
                baseButtonWidthDp * progress,
                baseButtonWidthDp * progress,
                progress
            )
        } else {
            val deepDenominator = (fullSwipeThresholdPx - actionButtonsWidthPx).coerceAtLeast(1f)
            val deepProgress = ((dragDistance - actionButtonsWidthPx) / deepDenominator).coerceIn(0f, 1f)
            val delWidth = if (deepProgress >= 1f) 0.dp else (baseButtonWidthDp * (1f - deepProgress)).coerceAtLeast(0.dp)
            val delAlpha = (1f - deepProgress).coerceIn(0f, 1f)
            val totalRevealedWidthDp = with(density) {
                (dragDistance - with(density) { 16.dp.toPx() }).toDp()
            }.coerceAtLeast(baseButtonWidthDp)
            val spacingDp = if (delWidth > 0.dp) 8.dp else 0.dp
            val edWidth = (totalRevealedWidthDp - delWidth - spacingDp).coerceAtLeast(baseButtonWidthDp)
            Triple(delWidth, edWidth, delAlpha)
        }
    }

    val buttonSpacingDp = if (deleteWidthDp > 0.dp) 8.dp else 0.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemWidthPx = it.width.toFloat() }
            .offset { IntOffset(slideAwayOffsetX.value.roundToInt(), 0) }
            .clip(shape)
    ) {
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacingDp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (deleteWidthDp > 0.dp) {
                Surface(
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
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(deleteWidthDp)
                        .graphicsLayer { alpha = deleteAlpha }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Holding"
                        )
                    }
                }
            }
            if (editWidthDp > 0.dp) {
                Surface(
                    onClick = onEdit,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(editWidthDp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Holding"
                        )
                    }
                }
            }
        }

        ListItem(
            headlineContent = {
                Text(
                    text = Formatters.formatDate(holding.purchaseDate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = "${holding.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    val displayPercent = if (holding.profitPercentage != BigDecimal.ZERO) {
                        holding.profitPercentage
                    } else {
                        holding.couponRate
                    }
                    if (displayPercent != BigDecimal.ZERO) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(
                                text = Formatters.formatPercentage(displayPercent),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = Formatters.formatAmount(holding.totalPaidAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Holding Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(shape)
                .pointerInput(actionButtonsWidthPx, isDeleting, canSwipe, fullSwipeThresholdPx, maxDragLeftPx) {
                    if (isDeleting) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = {
                            if (canSwipe) {
                                hasTriggeredRevealHaptic = isRevealed
                                onDragStart?.invoke()
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (!canSwipe) return@detectHorizontalDragGestures
                            coroutineScope.launch {
                                val current = offsetX.value
                                val minOffset = -maxDragLeftPx
                                val maxOffset = with(density) { 40.dp.toPx() }
                                val target = (current + dragAmount).coerceIn(minOffset, maxOffset)
                                offsetX.snapTo(target)

                                val currentDragDistance = -target
                                val revealThresholdPx = actionButtonsWidthPx / 3f

                                if (currentDragDistance >= revealThresholdPx) {
                                    if (!hasTriggeredRevealHaptic) {
                                        hasTriggeredRevealHaptic = true
                                        HapticFeedbackHelper.vibrateRevealThreshold(view)
                                    }
                                } else {
                                    if (hasTriggeredRevealHaptic) {
                                        hasTriggeredRevealHaptic = false
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            onDragEnd?.invoke()
                            coroutineScope.launch {
                                val currentDragDistance = -offsetX.value
                                if (currentDragDistance >= fullSwipeThresholdPx) {
                                    HapticFeedbackHelper.vibratePrimaryAction(view)
                                    onEdit()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                    onCollapse()
                                } else if (currentDragDistance >= actionButtonsWidthPx / 3) {
                                    onExpand()
                                    offsetX.animateTo(-actionButtonsWidthPx, animationSpec = swipeSpringSpec)
                                } else {
                                    onCollapse()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                }
                                hasTriggeredRevealHaptic = false
                            }
                        },
                        onDragCancel = {
                            onDragCancel?.invoke()
                            coroutineScope.launch {
                                val currentDragDistance = -offsetX.value
                                if (currentDragDistance >= fullSwipeThresholdPx) {
                                    HapticFeedbackHelper.vibratePrimaryAction(view)
                                    onEdit()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                    onCollapse()
                                } else if (currentDragDistance >= actionButtonsWidthPx / 3) {
                                    onExpand()
                                    offsetX.animateTo(-actionButtonsWidthPx, animationSpec = swipeSpringSpec)
                                } else {
                                    onCollapse()
                                    offsetX.animateTo(0f, animationSpec = swipeSpringSpec)
                                }
                                hasTriggeredRevealHaptic = false
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
                )
        )
    }
}
