package whatis.love.agedate.discover.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.likes.viewmodel.LikeViewModel
import whatis.love.agedate.ui.ProfileIcon

@Composable
fun ProfileCard(
    profile: Profile,
    likeViewModel: LikeViewModel,
    onStatusUpdate: (String, ProfileLikeStatus) -> Unit,
    onOpenDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val likeStatus by likeViewModel.observeLikeStatus(profile.id).collectAsState()

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = profile.profilePictureUrl,
                contentDescription = "Фотография профиля ${profile.firstName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush =
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 0f,
                                    endY = 300f,
                                ),
                        ),
            )

            if (profile.likedYouAt != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Симпатизирует вам",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Симпатизирует вам",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = profile.firstName,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = profile.lastName + profile.ageSuffix,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val isDisliked = likeStatus == ProfileLikeStatus.DISLIKED
                    val dislikeButtonColor by animateColorAsState(
                        targetValue =
                            if (isDisliked) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                Color.White
                            },
                        animationSpec = tween(durationMillis = 300),
                        label = "DislikeButtonColor",
                    )
                    val dislikeIconColor by animateColorAsState(
                        targetValue =
                            if (isDisliked) {
                                MaterialTheme.colorScheme.error
                            } else {
                                Color.Gray
                            },
                        animationSpec = tween(durationMillis = 300),
                        label = "DislikeIconColor",
                    )
                    val dislikeScale by animateFloatAsState(
                        targetValue = if (isDisliked) 1.1f else 1.0f,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        label = "DislikeScale",
                    )

                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingActionButton(
                            onClick = {
                                val newStatus =
                                    if (isDisliked) {
                                        ProfileLikeStatus.NONE
                                    } else {
                                        ProfileLikeStatus.DISLIKED
                                    }
                                onStatusUpdate(profile.id, newStatus)
                            },
                            containerColor = dislikeButtonColor,
                            contentColor = dislikeIconColor,
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .scale(dislikeScale),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription =
                                    if (isDisliked) {
                                        "Отменить неприязнь"
                                    } else {
                                        "Не нравится"
                                    },
                            )
                        }
                    }
                    val isLiked = likeStatus == ProfileLikeStatus.LIKED
                    val likeButtonColor by animateColorAsState(
                        targetValue =
                            if (isLiked) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.White
                            },
                        animationSpec = tween(durationMillis = 300),
                        label = "LikeButtonColor",
                    )
                    val likeIconColor by animateColorAsState(
                        targetValue =
                            if (isLiked) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        animationSpec = tween(durationMillis = 300),
                        label = "LikeIconColor",
                    )
                    val likeScale by animateFloatAsState(
                        targetValue = if (isLiked) 1.1f else 1.0f,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        label = "LikeScale",
                    )

                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingActionButton(
                            onClick = {
                                val newStatus =
                                    if (isLiked) {
                                        ProfileLikeStatus.NONE
                                    } else {
                                        ProfileLikeStatus.LIKED
                                    }
                                onStatusUpdate(profile.id, newStatus)
                            },
                            containerColor = likeButtonColor,
                            contentColor = likeIconColor,
                            modifier =
                                Modifier
                                    .size(56.dp)
                                    .scale(likeScale),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription =
                                    if (isLiked) {
                                        "Отменить симпатию"
                                    } else {
                                        "Нравится"
                                    },
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingActionButton(
                            onClick = { onOpenDetails(profile.id) },
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Подробнее",
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                ProfileIcon(
                    profileId = profile.id,
                )
            }

            FullScreenProfileIcon(
                profileId = profile.id,
                likeViewModel = likeViewModel,
            )
        }
    }
}
