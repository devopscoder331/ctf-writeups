package whatis.love.agedate.ui

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AnimatedGradient(gradientColors: List<Color>) {
    val transition = rememberInfiniteTransition(label = "gradientTransition")
    val translateAnimation =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(10000, easing = FastOutLinearInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "translateAnimation",
        )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush =
                Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(translateAnimation.value, translateAnimation.value),
                    end = Offset(translateAnimation.value + 1000f, translateAnimation.value + 1000f),
                ),
        )
    }
}
