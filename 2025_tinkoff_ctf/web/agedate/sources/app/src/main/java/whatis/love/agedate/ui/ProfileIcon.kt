package whatis.love.agedate.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.likes.viewmodel.LikeViewModel
import whatis.love.agedate.ui.theme.PeachPrimary
import whatis.love.agedate.visits.viewmodel.VisitTrackingViewModel

@Composable
fun ProfileIcon(
    profileId: String,
    size: Dp = 24.dp,
    padding: Dp = 16.dp,
) {
    val likeViewModel: LikeViewModel = hiltViewModel()
    val likeStatus by likeViewModel.observeLikeStatus(profileId).collectAsState()
    val trackingViewModel: VisitTrackingViewModel = hiltViewModel()
    val isVisited by trackingViewModel.observeProfileVisited(profileId).collectAsState()

    Box {
        PopIcon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Вам нравится",
            tint = PeachPrimary,
            visible = likeStatus == ProfileLikeStatus.LIKED,
            size = size,
            padding = padding,
        )

        PopIcon(
            imageVector = Icons.Default.Close,
            contentDescription = "Вам не нравится",
            tint = PeachPrimary,
            visible = likeStatus == ProfileLikeStatus.DISLIKED,
            size = size,
            padding = padding,
        )

        PopIcon(
            imageVector = Icons.Default.NewReleases,
            contentDescription = "Анкета ещё не просмотрена",
            tint = PeachPrimary,
            visible = likeStatus == ProfileLikeStatus.NONE && !isVisited,
            size = size,
            padding = padding,
        )
    }
}
