package whatis.love.agedate.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PopIcon(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    padding: Dp = 16.dp,
    popInAnimationSpec: SpringSpec<Float> =
        spring(
            dampingRatio = 0.3f,
            stiffness = Spring.StiffnessLow,
        ),
    visible: Boolean,
) {
    val scale = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(key1 = visible) {
        delay(50)
        if (!visible) {
            scale.animateTo(
                targetValue = 0f,
                initialVelocity = 2f,
                animationSpec = tween(300, easing = EaseOut),
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = popInAnimationSpec,
            )
        }
    }

    Icon(
        modifier =
            modifier
                .padding(padding)
                .size(size)
                .scale(scale.value),
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
    )
}
