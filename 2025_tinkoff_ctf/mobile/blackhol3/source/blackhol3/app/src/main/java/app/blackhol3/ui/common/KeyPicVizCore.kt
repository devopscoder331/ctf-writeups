package app.blackhol3.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

enum class TransitionMode {
    NORMAL,
    BLINK,
    FILL,
    CLEAR,
}

fun calculateWaveDelays(
    blocks: Int,
    direction: Int = 0,
    delayFactor: Int = 70,
): Array<IntArray> {
    val delays = Array(blocks) { IntArray(blocks) }

    for (x in 0 until blocks) {
        for (y in 0 until blocks) {
            val delay =
                when (direction) {
                    0 -> y
                    1 -> blocks - 1 - y
                    2 -> x
                    3 -> blocks - 1 - x
                    4 -> x + y
                    5 -> (blocks - 1 - x) + (blocks - 1 - y)
                    6 -> (blocks - 1 - x) + y
                    7 -> x + (blocks - 1 - y)
                    else -> 0
                }

            delays[x][y] = delay * delayFactor
        }
    }

    return delays
}

fun getTransformOrigin(direction: Int): TransformOrigin =
    when (direction) {
        0 -> TransformOrigin(0.5f, 0f)
        1 -> TransformOrigin(0.5f, 1f)
        2 -> TransformOrigin(0f, 0.5f)
        3 -> TransformOrigin(1f, 0.5f)
        4 -> TransformOrigin(0f, 0f)
        5 -> TransformOrigin(1f, 1f)
        6 -> TransformOrigin(1f, 0f)
        7 -> TransformOrigin(0f, 1f)
        else -> TransformOrigin.Center
    }

@Composable
fun KeyPicVizGrid(
    pattern: Array<BooleanArray>,
    invert: Boolean,
    maxSize: Int,
    animate: Boolean,
    transitionMode: TransitionMode,
    color: Color,
    previousColor: Color? = null,
    animationTrigger: Int,
    blinkDuration: Int = 300,
    appearDuration: Int = 300,
    disappearDuration: Int = 300,
    onAnimationComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    waveDirection: Int,
    waveDelayFactor: Int = 15,
) {
    val patternSize = pattern.size
    val offset = (maxSize - patternSize) / 2

    val transformOrigin = remember(waveDirection) { getTransformOrigin(waveDirection) }

    val delays =
        remember(waveDirection, maxSize, waveDelayFactor) {
            calculateWaveDelays(maxSize, waveDirection, delayFactor = waveDelayFactor)
        }

    val maxDelay =
        remember(delays) {
            delays.maxOf { row -> row.maxOrNull() ?: 0 }
        }

    val totalDuration =
        remember(transitionMode, maxDelay, appearDuration, disappearDuration) {
            when (transitionMode) {
                TransitionMode.BLINK -> maxDelay + blinkDuration + appearDuration + 100
                else -> maxDelay + maxOf(appearDuration, disappearDuration) + 100
            }
        }

    LaunchedEffect(animationTrigger, animate) {
        if (animate) {
            delay(totalDuration.toLong())
            onAnimationComplete()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1f),
    ) {
        for (x in 0 until maxSize) {
            Row(modifier = Modifier.weight(1f)) {
                for (y in 0 until maxSize) {
                    Box(modifier = Modifier.weight(1f)) {
                        val animDelay = delays[x][y].toLong()

                        val inPatternBounds =
                            x >= offset &&
                                x < offset + patternSize &&
                                y >= offset &&
                                y < offset + patternSize

                        val isEnabled =
                            if (inPatternBounds) {
                                invert xor pattern[x - offset][y - offset]
                            } else {
                                invert
                            }

                        KeyPicVizPixel(
                            shouldBeVisible = isEnabled,
                            animationDelay = animDelay,
                            color = color,
                            previousColor = previousColor,
                            isAnimationActive = animate,
                            transitionMode = transitionMode,
                            animationTrigger = animationTrigger,
                            blinkDuration = blinkDuration,
                            appearDuration = appearDuration,
                            disappearDuration = disappearDuration,
                            transformOrigin = transformOrigin,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeyPicVizPixel(
    shouldBeVisible: Boolean,
    animationDelay: Long,
    color: Color,
    previousColor: Color? = null,
    isAnimationActive: Boolean,
    transitionMode: TransitionMode = TransitionMode.NORMAL,
    animationTrigger: Int = 0,
    blinkDuration: Int = 300,
    appearDuration: Int = 300,
    disappearDuration: Int = 300,
    colorChangeDuration: Int = 800,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }
    var previousVisibility by remember { mutableStateOf(false) }

    var targetColor by remember { mutableStateOf(color) }
    var startColor by remember { mutableStateOf(previousColor ?: color) }

    var invertedTransformOrigin =
        remember(transformOrigin) {
            TransformOrigin(
                pivotFractionX = 1f - transformOrigin.pivotFractionX,
                pivotFractionY = 1f - transformOrigin.pivotFractionY,
            )
        }

    LaunchedEffect(color, previousColor) {
        startColor = previousColor ?: targetColor
        targetColor = color
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = colorChangeDuration),
        label = "color",
    )

    LaunchedEffect(animationTrigger, isAnimationActive) {
        if (isAnimationActive) {
            previousVisibility = isVisible
            delay(animationDelay)
            when (transitionMode) {
                TransitionMode.BLINK -> {
                    isVisible = true

                    delay(blinkDuration.toLong())

                    isVisible = shouldBeVisible
                }

                TransitionMode.FILL -> {
                    isVisible = true
                }

                TransitionMode.CLEAR -> {
                    isVisible = false
                }

                else -> {
                    isVisible = shouldBeVisible
                }
            }
        }
    }

    LaunchedEffect(shouldBeVisible) {
        if (!isAnimationActive) {
            previousVisibility = isVisible
            isVisible = shouldBeVisible
        }
    }

    val currentTransformOrigin =
        remember(isVisible, previousVisibility) {
            if (!isVisible && previousVisibility) {
                invertedTransformOrigin
            } else {
                transformOrigin
            }
        }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec =
            if (isVisible) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessVeryLow,
                )
            },
        label = "scale",
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec =
            if (isVisible) {
                tween(durationMillis = appearDuration)
            } else {
                tween(durationMillis = disappearDuration)
            },
        label = "alpha",
    )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    this.transformOrigin = currentTransformOrigin
                }.background(animatedColor),
    )
}
