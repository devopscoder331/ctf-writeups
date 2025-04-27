package whatis.love.agedate.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import whatis.love.agedate.profile.ui.MicrostatusShape
import whatis.love.agedate.ui.ProfileIcon

@Composable
fun FeaturedProfileCard(
    profile: Profile,
    modifier: Modifier = Modifier,
    imageModifier: Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 6.dp,
            ),
        onClick = onClick,
    ) {
        Box {
            AsyncImage(
                model = profile.profilePictureUrl,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = imageModifier,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f),
                                    ),
                                startY = 0f,
                                endY = 300f,
                            ),
                        ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                ProfileIcon(
                    profileId = profile.id,
                )
            }

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                val microstatus = profile.microstatus
                if (microstatus != null) {
                    Surface(
                        shape =
                            MicrostatusShape(
                                cornerRadius = 12f,
                                arrowPosition = MicrostatusShape.ArrowPosition.BOTTOM_START,
                                arrowWidth = 24f,
                                arrowHeight = 14f,
                            ),
                        color =
                            MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.90f,
                            ),
                        shadowElevation = 2.dp,
                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                bottom = 8.dp,
                                end = 16.dp,
                                top = 8.dp,
                            ),
                    ) {
                        Text(
                            text = microstatus.text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier =
                                Modifier.padding(
                                    start = 10.dp,
                                    end = 12.dp,
                                    top = 10.dp,
                                    bottom = 10.dp,
                                ),
                        )
                    }
                }
                Text(
                    text = "${profile.firstName} ${profile.lastName}${profile.ageSuffix}",
                    style = MaterialTheme.typography.titleMedium,
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
        }
    }
}
