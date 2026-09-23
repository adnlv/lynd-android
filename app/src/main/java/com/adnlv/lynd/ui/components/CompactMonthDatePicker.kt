package com.adnlv.lynd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private const val BASE_YEAR = 2000
private const val TOTAL_CALENDAR_MONTHS = 2400

private fun pageFromYearMonth(ym: YearMonth): Int =
    (ym.year - BASE_YEAR) * 12 + (ym.monthValue - 1)

private fun yearMonthFromPage(page: Int): YearMonth =
    YearMonth.of(BASE_YEAR + page / 12, (page % 12) + 1)

@Composable
fun CompactMonthDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState(
        initialPage = remember(selectedDate) {
            pageFromYearMonth(YearMonth.from(selectedDate))
        },
        pageCount = { TOTAL_CALENDAR_MONTHS }
    )
) {
    val coroutineScope = rememberCoroutineScope()

    val currentYearMonth = remember(pagerState.currentPage) {
        yearMonthFromPage(pagerState.currentPage)
    }

    var isMonthYearPickerVisible by remember { mutableStateOf(false) }
    var pickerYear by remember(currentYearMonth) { mutableStateOf(currentYearMonth.year) }

    val daysOfWeek = remember {
        listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    }

    val monthTitle = remember(currentYearMonth) {
        val monthName = currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "$monthName ${currentYearMonth.year}"
    }

    val goToToday: () -> Unit = {
        val today = LocalDate.now()
        val targetPage = pageFromYearMonth(YearMonth.from(today))
        isMonthYearPickerVisible = false
        pickerYear = today.year
        coroutineScope.launch {
            pagerState.animateScrollToPage(targetPage)
        }
        onDateSelected(today)
    }

    val calendarNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (abs(consumed.x) > 0f) {
                    Offset(x = 0f, y = available.y)
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                return if (abs(consumed.x) > 0f) {
                    Velocity(x = 0f, y = available.y)
                } else {
                    Velocity.Zero
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                RoundedCornerShape(16.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .nestedScroll(calendarNestedScrollConnection)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (isMonthYearPickerVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { pickerYear -= 1 },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Year"
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { isMonthYearPickerVisible = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pickerYear.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(
                        onClick = goToToday,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Go to Today",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = { pickerYear += 1 },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Year"
                    )
                }
            }

            val monthNames = remember {
                (1..12).map { m ->
                    YearMonth.of(2000, m).month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (rowIndex in 0 until 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (colIndex in 0 until 3) {
                            val monthValue = rowIndex * 3 + colIndex + 1
                            val isSelectedMonth = monthValue == currentYearMonth.monthValue && pickerYear == currentYearMonth.year
                            val monthText = monthNames[monthValue - 1]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelectedMonth) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerHigh
                                        }
                                    )
                                    .clickable {
                                        val targetYearMonth = YearMonth.of(pickerYear, monthValue)
                                        val targetPage = pageFromYearMonth(targetYearMonth)
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(targetPage)
                                        }
                                        isMonthYearPickerVisible = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelectedMonth) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelectedMonth) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable {
                                pickerYear = currentYearMonth.year
                                isMonthYearPickerVisible = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = goToToday,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Go to Today",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (pagerState.currentPage < TOTAL_CALENDAR_MONTHS - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                daysOfWeek.forEachIndexed { index, dayName ->
                    val isWeekendHeader = index >= 5
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isWeekendHeader) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageYearMonth = yearMonthFromPage(page)
                val firstDayOfMonth = pageYearMonth.atDay(1)
                val firstDayOfWeekIndex = firstDayOfMonth.dayOfWeek.value - 1
                val gridStartDate = firstDayOfMonth.minusDays(firstDayOfWeekIndex.toLong())

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (rowIndex in 0 until 6) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (colIndex in 0 until 7) {
                                val cellIndex = rowIndex * 7 + colIndex
                                val cellDate = gridStartDate.plusDays(cellIndex.toLong())
                                val isCurrentMonth = cellDate.month == pageYearMonth.month && cellDate.year == pageYearMonth.year
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == LocalDate.now()
                                val isWeekend = colIndex >= 5

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .clickable {
                                                onDateSelected(cellDate)
                                                if (!isCurrentMonth) {
                                                    val targetPage = pageFromYearMonth(YearMonth.from(cellDate))
                                                    if (targetPage in 0 until TOTAL_CALENDAR_MONTHS) {
                                                        coroutineScope.launch {
                                                            pagerState.animateScrollToPage(targetPage)
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cellDate.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || (isToday && isCurrentMonth)) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                !isCurrentMonth && isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                                                !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                                isToday -> MaterialTheme.colorScheme.primary
                                                isWeekend -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
