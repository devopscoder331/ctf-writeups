package whatis.love.agedate.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.ui.ProfileIcon

@Composable
fun SmallProfileCard(
    profile: Profile,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .fillMaxHeight()
                .padding(end = 8.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 3.dp,
            ),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = profile.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier =
                    Modifier
                        .fillMaxSize(),
            )
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f),
                                    ),
                                startY = 0f,
                                endY = 500f,
                            ),
                        ),
            )

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.matchParentSize(),
            ) {
                Text(
                    text = "${profile.firstName}${profile.ageSuffix}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Serif,
                    color = Color.White,
                    modifier =
                        Modifier
                            .padding(
                                start = 16.dp,
                                bottom = 16.dp,
                                end = 16.dp,
                            ).fillMaxWidth(),
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                if (profile.likeStatus != ProfileLikeStatus.NONE) {
                    if (profile.likeStatus == ProfileLikeStatus.LIKED) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Вам понравился профиль",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .size(24.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Вам не понравился профиль",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .size(24.dp),
                        )
                    }
                } else {
                    ProfileIcon(
                        profileId = profile.id,
                        padding = 0.dp,
                    )
                }
            }
        }
    }
}
