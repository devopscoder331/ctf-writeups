package whatis.love.agedate.discover.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.likes.viewmodel.LikeViewModel

@Composable
fun FullScreenProfileIcon(
    profileId: String,
    likeViewModel: LikeViewModel,
    modifier: Modifier = Modifier,
) {
    val likeStatus by likeViewModel.observeLikeStatus(profileId).collectAsState()
    val previousLikeStatus = remember { mutableStateOf<ProfileLikeStatus?>(null) }
    val showIcon = remember { mutableStateOf(false) }
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(likeStatus) {
        if (previousLikeStatus.value != null &&
            previousLikeStatus.value != likeStatus &&
            (likeStatus == ProfileLikeStatus.LIKED || likeStatus == ProfileLikeStatus.DISLIKED)
        ) {
            showIcon.value = true
            scale.snapTo(0f)
            alpha.snapTo(0f)
            coroutineScope {
                launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = 400,
                                easing = FastOutSlowInEasing,
                            ),
                    )
                    showIcon.value = false
                }

                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = 120,
                                easing = FastOutSlowInEasing,
                            ),
                    )
                    delay(30)
                    alpha.animateTo(
                        targetValue = 0f,
                        animationSpec =
                            tween(
                                durationMillis = 150,
                                easing = FastOutLinearInEasing,
                            ),
                    )
                }
            }
        }
        previousLikeStatus.value = likeStatus
    }

    Box(
        modifier =
            modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (showIcon.value) {
            if (likeStatus == ProfileLikeStatus.LIKED) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Вам нравится",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier =
                        Modifier
                            .size(300.dp)
                            .scale(scale.value)
                            .alpha(alpha.value),
                )
            }
            if (likeStatus == ProfileLikeStatus.DISLIKED) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Вам не нравится",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier =
                        Modifier
                            .size(300.dp)
                            .scale(scale.value)
                            .alpha(alpha.value),
                )
            }
        }
    }
}
