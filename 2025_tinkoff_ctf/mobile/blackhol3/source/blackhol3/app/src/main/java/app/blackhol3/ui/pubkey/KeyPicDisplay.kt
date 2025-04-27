package app.blackhol3.ui.pubkey

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.blackhol3.model.KeyPicVizData
import app.blackhol3.service.KeyPicGenerationService
import app.blackhol3.ui.common.KeyPicVizGrid
import app.blackhol3.ui.common.TransitionMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun KeyPicDisplay(
    keyPicVizData: KeyPicVizData?,
    keyPicGenService: KeyPicGenerationService = koinInject(),
) {
    val placeholderData =
        remember {
            KeyPicVizData(
                15,
                keyPicGenService.generateColor(Random(System.currentTimeMillis())),
                key,
            )
        }
    var animationState by remember { mutableStateOf(AnimationState()) }
    var autoAnimationActive by remember { mutableStateOf(false) }
    var initialAnimationDone by remember { mutableStateOf(false) }
    var initialAnimationJob: Job? by remember { mutableStateOf<Job?>(null) }

    val direction = remember { (4..7).random() }

    val displayData by remember(
        animationState.pattern,
        animationState.color,
        animationState.inverted,
    ) {
        derivedStateOf {
            KeyPicVizData(
                blocks = 15,
                color = animationState.color,
                pattern = animationState.pattern,
            )
        }
    }

    LaunchedEffect(Unit) {
        initialAnimationJob =
            launch {
                delay(1000)

                animationState =
                    animationState.copy(
                        pattern = placeholderData.pattern,
                        color = placeholderData.color,
                        transitionMode = TransitionMode.BLINK,
                        animationTrigger = animationState.animationTrigger + 1,
                        isAnimating = true,
                    )

                delay(1200)

                initialAnimationDone = true
                autoAnimationActive = keyPicVizData == null
            }
    }

    LaunchedEffect(keyPicVizData) {
        initialAnimationJob?.cancel()

        if (keyPicVizData != null) {
            val newPattern = keyPicVizData.pattern
            val newColor = keyPicVizData.color

            val hasChanged =
                !newPattern.contentDeepEquals(animationState.pattern) ||
                    newColor != animationState.color

            if (hasChanged) {
                autoAnimationActive = false

                while (animationState.isAnimating) {
                    delay(200)
                }

                animationState =
                    animationState.copy(
                        color = newColor,
                        pattern = newPattern,
                        transitionMode = TransitionMode.BLINK,
                        animationTrigger = animationState.animationTrigger + 1,
                        isAnimating = true,
                        inverted = false,
                    )
            }
        } else if (!autoAnimationActive) {
            autoAnimationActive = true

            animationState =
                animationState.copy(
                    pattern = placeholderData.pattern,
                    color = placeholderData.color,
                    transitionMode = TransitionMode.BLINK,
                    animationTrigger = animationState.animationTrigger + 1,
                    isAnimating = true,
                    inverted = false,
                )
        }
    }

    LaunchedEffect(autoAnimationActive) {
        while (autoAnimationActive) {
            if (animationState.isAnimating) {
                delay(100)
                continue
            }

            delay(2000)

            if (!autoAnimationActive) break

            animationState =
                animationState.copy(
                    pattern = placeholderData.pattern,
                    inverted = !animationState.inverted,
                    transitionMode = TransitionMode.NORMAL,
                    animationTrigger = animationState.animationTrigger + 1,
                    isAnimating = true,
                )
        }
    }

    Box(
        modifier = Modifier.Companion.size(200.dp),
        contentAlignment = Alignment.Companion.Center,
    ) {
        KeyPicVizGrid(
            pattern = displayData.pattern,
            invert = animationState.inverted,
            maxSize = 17,
            animate = animationState.isAnimating,
            transitionMode = animationState.transitionMode,
            waveDirection = direction,
            color = Color(displayData.color),
            animationTrigger = animationState.animationTrigger,
            blinkDuration = 600,
            appearDuration = 50,
            disappearDuration = 50,
            onAnimationComplete = {
                animationState = animationState.copy(isAnimating = false)
            },
            modifier = Modifier.Companion.fillMaxSize(),
        )
    }
}
