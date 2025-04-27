package whatis.love.agedate.discover.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spartapps.swipeablecards.state.rememberSwipeableCardsState
import com.spartapps.swipeablecards.ui.SwipeableCardDirection
import com.spartapps.swipeablecards.ui.SwipeableCardsProperties
import com.spartapps.swipeablecards.ui.lazy.LazySwipeableCards
import com.spartapps.swipeablecards.ui.lazy.items
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.discover.viewmodel.DiscoverViewModel
import whatis.love.agedate.likes.viewmodel.LikeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    discoverViewModel: DiscoverViewModel,
    likeViewModel: LikeViewModel,
    onDetailsClick: (String) -> Unit = {},
    navbarPaddingValues: PaddingValues,
) {
    val uiState = discoverViewModel.uiState.collectAsStateWithLifecycle()
    val profiles = uiState.value.profiles

    val state =
        rememberSwipeableCardsState(
            initialCardIndex = 0,
            itemCount = { profiles.size },
        )

    fun like(profile: Profile) {
        likeViewModel.like(profile.id)
        discoverViewModel.fetchMore()
    }

    fun dislike(profile: Profile) {
        likeViewModel.dislike(profile.id)
        discoverViewModel.fetchMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fontWeight = FontWeight.Bold, text = "Картотека") },
                actions = {
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
            )
        },
    ) { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .padding(bottom = navbarPaddingValues.calculateBottomPadding()),
        ) {
            LazySwipeableCards<Profile>(
                properties =
                    SwipeableCardsProperties(
                        stackedCardsOffset = 1.dp,
                        padding = 0.dp,
                    ),
                state = state,
                onSwipe = { profile, direction ->
                    when (direction) {
                        SwipeableCardDirection.Right -> {
                            like(profile)
                        }

                        SwipeableCardDirection.Left -> {
                            dislike(profile)
                        }
                    }
                },
            ) {
                items(profiles) { profile, index, offset ->
                    ProfileCard(
                        profile = profile,
                        likeViewModel = likeViewModel,
                        onStatusUpdate = { profileID, status ->
                            likeViewModel.setLikeStatus(profileID, status)
                        },
                        onOpenDetails = { onDetailsClick(profile.id) },
                    )
                }
            }
        }
    }
}
