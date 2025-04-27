package app.blackhol3

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.ceil
import kotlin.math.min

data class ByteElement(
    val value: Int,
    val hexString: String,
)

@Composable
fun ByteGridBackground(
    byteFlow: Flow<Byte?>,
    modifier: Modifier = Modifier,
    cellSize: Int = 24,
    content: @Composable BoxWithConstraintsScope.(() -> Unit) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val density = LocalDensity.current

        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }
        val cellSizePx = with(density) { cellSize.dp.toPx() }

        val columnsNeeded = ceil(screenWidthPx / cellSizePx).toInt()
        val rowsNeeded = ceil(screenHeightPx / cellSizePx).toInt()
        val totalCellsNeeded = columnsNeeded * rowsNeeded

        val byteElements =
            remember {
                val baseElements =
                    (0..255)
                        .map { value ->
                            ByteElement(
                                value = value,
                                hexString = value.toString(16).padStart(2, '0').uppercase(),
                            )
                        }.shuffled()

                val repeatedElements = mutableListOf<ByteElement>()
                while (repeatedElements.size < totalCellsNeeded) {
                    repeatedElements.addAll(baseElements)
                }

                repeatedElements.take(totalCellsNeeded)
            }

        val cellCounters =
            remember {
                Array(totalCellsNeeded) { MutableStateFlow(0) }
            }

        val bytePosLookup =
            remember {
                val lookup = Array(256) { mutableListOf<Int>() }
                byteElements.forEachIndexed { index, element ->
                    lookup[element.value].add(index)
                }
                lookup
            }

        LaunchedEffect(key1 = byteFlow) {
            byteFlow.filterNotNull().collect { byte ->
                val byteValue = byte.toInt() and 0xFF

                bytePosLookup[byteValue].forEach { position ->
                    if (position < cellCounters.size) {
                        cellCounters[position].value += 1
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnsNeeded),
            modifier =
                Modifier
                    .fillMaxSize()
                    .zIndex(0f)
                    .alpha(0.4f),
            userScrollEnabled = false,
        ) {
            items(byteElements.size, key = { it }) { index ->
                BackgroundByteGridCell(
                    byte = byteElements[index],
                    counterFlow = cellCounters[index],
                    modifier =
                        Modifier
                            .padding(1.dp)
                            .aspectRatio(1f),
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            contentAlignment = Alignment.Center,
        ) {
            this@BoxWithConstraints.content({
                cellCounters.forEach {
                    it.value = 0
                }
            })
        }
    }
}

@Composable
private fun BackgroundByteGridCell(
    byte: ByteElement,
    counterFlow: Flow<Int>,
    modifier: Modifier = Modifier,
) {
    var counter by remember { mutableIntStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(counterFlow) {
        counterFlow.collect { newCount ->
            if (newCount > counter || (counter > 0 && newCount == 0)) {
                counter = newCount

                isAnimating = true

                delay(300)
                isAnimating = false
            } else {
                counter = newCount
            }
        }
    }

    val saturation =
        if (counter > 0) {
            min(0.6f + (counter.toFloat() * 0.05f), 1f)
        } else {
            0f
        }

    val textColor =
        Color.hsv(
            hue = 120f,
            saturation = saturation,
            value = 1f,
        )

    val animatedTextAlpha by animateFloatAsState(
        targetValue =
            when {
                isAnimating -> 1f
                counter > 0 -> 0.7f
                else -> 0.0f
            },
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "alpha",
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 0.7f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "alpha",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        if (isAnimating) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Color
                                .hsv(
                                    hue = 90f,
                                    saturation = 1f,
                                    value = 1f,
                                ).copy(alpha = 0.15f),
                        ).zIndex(0f)
                        .alpha(animatedAlpha),
            )
        }

        Text(
            text = byte.hexString,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
            modifier =
                Modifier
                    .zIndex(1f)
                    .alpha(animatedTextAlpha),
        )
    }
}
