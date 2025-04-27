package whatis.love.agedate.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import whatis.love.agedate.R
import whatis.love.agedate.home.viewmodel.HomeScreenViewModel
import whatis.love.agedate.ui.AnimatedGradient
import whatis.love.agedate.ui.theme.PeachDarker
import whatis.love.agedate.ui.theme.PeachSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToProfile: (String) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToQRScanner: () -> Unit,
    navbarPaddingValues: PaddingValues,
) {
    val uiState = homeScreenViewModel.uiState.collectAsStateWithLifecycle()
    val content = uiState.value.content

    Scaffold(
        topBar = {
            Surface(shadowElevation = 3.dp) {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.agedate_logo_hollow),
                            contentDescription = "Логотип",
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier =
                                Modifier
                                    .padding(vertical = 12.dp)
                                    .offset(y = 3.dp),
                        )
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToQRScanner() }) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Отсканировать QR-код",
                            )
                        }
                    },
                    windowInsets = WindowInsets.statusBars,
                    colors = TopAppBarDefaults.topAppBarColors(),
                    modifier = Modifier.statusBarsPadding(),
                )
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.value.refreshing,
            onRefresh = {
                homeScreenViewModel.refresh()
            },
            modifier =
                Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                    ).fillMaxSize(),
        ) {
            if (uiState.value.loading && content == null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = navbarPaddingValues.calculateBottomPadding()),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                return@PullToRefreshBox
            }

            if (content == null) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = navbarPaddingValues.calculateBottomPadding(),
                            ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Не удалось загрузить главную страницу: ${uiState.value.error}")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { homeScreenViewModel.refresh(false) }) {
                        Text("Повторить")
                    }
                }
                return@PullToRefreshBox
            }

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
            ) {
                items(items = content.announcements, key = { it.hashCode() }) {
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                text = it.text,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                item {
                    if (content.announcements.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        onClick = onNavigateToDiscover,
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    ) {
                        Column {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier =
                                        Modifier
                                            .matchParentSize(),
                                ) {
                                    AnimatedGradient(
                                        gradientColors =
                                            listOf(
                                                PeachSecondary,
                                                PeachDarker,
                                            ),
                                    )
                                }

                                Column {
                                    Column(
                                        modifier =
                                            Modifier.padding(
                                                start = 24.dp,
                                                top = 16.dp,
                                            ),
                                    ) {
                                        Text(
                                            text = "Найдите новых друзей",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                        )

                                        Text(
                                            text = "в нашей картотеке",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White,
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            text = "Нажмите здесь, чтобы открыть",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White,
                                            modifier =
                                                Modifier.padding(
                                                    start = 24.dp,
                                                    bottom = 16.dp,
                                                ),
                                        )

                                        Image(
                                            imageVector = Icons.Outlined.PhotoLibrary,
                                            colorFilter = ColorFilter.tint(Color.White),
                                            contentDescription = "Стопка фотокарточек",
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        bottom = 16.dp,
                                                    ).size(90.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(8.dp))

                    Text(
                        text = "Пользователи пишут",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    FeaturedProfileCard(
                        profile = content.featuredProfile,
                        imageModifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 210.dp),
                        modifier = Modifier.height(210.dp),
                        onClick = {
                            onNavigateToProfile(content.featuredProfile.id)
                        },
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(16.dp))

                    Text(
                        text = "Возможно, вам понравятся",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    LazyRow(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                    ) {
                        item {
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        items(items = content.randomProfiles, key = { it.hashCode() }) {
                            SmallProfileCard(
                                profile = it,
                                onClick = {
                                    onNavigateToProfile(it.id)
                                },
                                modifier =
                                    Modifier
                                        .height(192.dp)
                                        .width(140.dp),
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(navbarPaddingValues.calculateBottomPadding() + 8.dp))
                }
            }
        }
    }
}
