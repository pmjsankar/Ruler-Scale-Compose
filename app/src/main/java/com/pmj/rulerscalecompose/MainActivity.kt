package com.pmj.rulerscalecompose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pmj.rulerscalecompose.RulerConstants.ItemHeightDp
import com.pmj.rulerscalecompose.RulerConstants.LabelPaddingEnd
import com.pmj.rulerscalecompose.RulerConstants.RulerPadding
import com.pmj.rulerscalecompose.RulerConstants.TickAreaWidth
import com.pmj.rulerscalecompose.ui.theme.RulerScaleComposeTheme
import kotlin.math.abs
import kotlin.math.roundToInt


private const val UNIT_CM = "cm"
private const val UNIT_FT = "ft"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RulerScaleComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RulerScaleWithNeedle(innerPadding) { _, _ ->
                        // Observe selected value and unit here
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RulerScaleWithNeedle(
    paddingValues: PaddingValues,
    maxCm: Int = 215,
    maxFt: Int = 7 * 12,
    minValue: Int = 0,
    onValueChange: (Int, String) -> Unit
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val density = LocalDensity.current
    val itemHeightPx = with(density) { ItemHeightDp.toPx() }

    var unit by remember { mutableStateOf(UNIT_CM) }
    val maxValue = if (unit == UNIT_CM) maxCm else maxFt
    var selectedValue by remember { mutableIntStateOf(maxValue) }
    var previousValue by remember { mutableIntStateOf(-1) }
    val haptic = LocalHapticFeedback.current

    val colors = rememberRulerColors()

    BoxWithConstraints(
        modifier = Modifier
            .background(colors.background)
            .padding(paddingValues)
    ) {
        RulerScaleContent(
            listState = listState,
            flingBehavior = flingBehavior,
            unit = unit,
            maxValue = maxValue,
            minValue = minValue,
            selectedValue = selectedValue,
            colors = colors,
            onUnitChange = { unit = it }
        )

        LaunchedEffect(unit) {
            collectSelectedValue(
                listState, itemHeightPx, maxValue, minValue
            ) { newValue ->
                if (newValue != previousValue) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    previousValue = newValue
                }
                selectedValue = newValue
                onValueChange(newValue, unit)
            }
        }
    }
}

@Composable
private fun RulerScaleContent(
    listState: LazyListState,
    flingBehavior: FlingBehavior,
    unit: String,
    maxValue: Int,
    minValue: Int,
    selectedValue: Int,
    colors: RulerColors,
    onUnitChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = RulerPadding)
    ) {
        RulerTicks(
            listState,
            flingBehavior,
            unit,
            maxValue,
            minValue,
            selectedValue,
            colors
        )
        UnitToggle(selectedUnit = unit, onUnitChange)
        NeedleLine(
            color = colors.needle,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        SelectedValueText(
            value = selectedValue,
            unit = unit,
            color = colors.text,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, bottom = 40.dp)
        )

    }
}

@Composable
private fun RulerTicks(
    listState: LazyListState,
    flingBehavior: FlingBehavior,
    unit: String,
    maxValue: Int,
    minValue: Int,
    selectedValue: Int,
    colors: RulerColors
) {

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 220.dp)
    ) {
        items((maxValue downTo minValue).toList()) { value ->
            TickItem(
                value,
                unit,
                selectedValue,
                colors
            )
        }
    }
}

@Composable
private fun TickItem(
    value: Int,
    unit: String,
    selectedValue: Int,
    colors: RulerColors
) {
    val isMajorTick = if (unit == UNIT_CM) value % 10 == 0 else value % 12 == 0
    val isHighlighted = value == selectedValue

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ItemHeightDp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Canvas(
            modifier = Modifier
                .width(TickAreaWidth)
                .fillMaxHeight()
                .padding(end = 16.dp)
        ) {
            val lineLength = when {
                isHighlighted -> size.width * 0.9f
                isMajorTick -> size.width * 0.7f
                else -> size.width * 0.3f
            }
            drawLine(
                color = if (isHighlighted) colors.needle else colors.tick,
                start = Offset(size.width, size.height / 2),
                end = Offset(size.width - lineLength, size.height / 2),
                strokeWidth = if (isHighlighted) 4f else if (isMajorTick) 3f else 1.5f
            )
        }

        if (isMajorTick) {
            val label = if (unit == UNIT_FT) "${value / 12}′" else "$value"
            Text(
                text = label,
                fontSize = 12.sp,
                color = colors.text,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = LabelPaddingEnd)
                    .wrapContentHeight(align = Alignment.CenterVertically, unbounded = true)
            )
        }
    }
}

@Composable
private fun NeedleLine(color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.5f)
            .height(2.dp)
            .background(color)
    )
}

@Composable
private fun SelectedValueText(value: Int, unit: String, color: Color, modifier: Modifier) {
    Text(
        text = if (unit == UNIT_CM) "$value cm" else formatFeetInches(value),
        color = color,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun rememberRulerColors(): RulerColors {
    return RulerColors(
        background = MaterialTheme.colorScheme.background,
        tick = MaterialTheme.colorScheme.onBackground,
        needle = MaterialTheme.colorScheme.primary,
        text = MaterialTheme.colorScheme.onBackground
    )
}


private suspend fun collectSelectedValue(
    listState: LazyListState,
    itemHeightPx: Float,
    maxValue: Int,
    minValue: Int,
    onNewValue: (Int) -> Unit
) {
    snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
        .collect {
            val centerY = listState.layoutInfo.viewportStartOffset +
                    (listState.layoutInfo.viewportEndOffset - listState.layoutInfo.viewportStartOffset) / 2

            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val closest = visibleItems.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - centerY)
            }

            closest?.let {
                val diffPx = centerY - (it.offset + it.size / 2)
                val diffUnit = diffPx / itemHeightPx
                val exactValue = (maxValue - it.index) + diffUnit
                val newValue = exactValue.roundToInt().coerceIn(minValue, maxValue)
                onNewValue(newValue)
            }
        }
}


/** Format inches to feet′ inches″ */
fun formatFeetInches(totalInches: Int): String {
    val feet = totalInches / 12
    val inches = totalInches % 12
    return "${feet}′ ${inches}″"
}

data class RulerColors(
    val background: Color,
    val tick: Color,
    val needle: Color,
    val text: Color
)

object RulerConstants {
    val ItemHeightDp = 10.dp
    val TickAreaWidth = 80.dp
    val LabelPaddingEnd = 90.dp
    val RulerPadding = 152.dp
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitToggle(selectedUnit: String, onUnitChange: (String) -> Unit) {
    val options = listOf(UNIT_CM, UNIT_FT)

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.padding(start = 16.dp),
    ) {
        options.forEachIndexed { index, unit ->
            SegmentedButton(
                selected = selectedUnit == unit,
                onClick = { onUnitChange(unit) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                label = { Text(unit) }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, heightDp = 600)
@Composable
fun RulerScaleWithNeedlePreview() {
    RulerScaleWithNeedle(
        PaddingValues(),
        maxCm = 215,
        maxFt = 7 * 12,
        minValue = 0
    ) { _, _ ->
        // Preview - no-op
    }
}
