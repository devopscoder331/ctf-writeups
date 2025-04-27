package whatis.love.agedate.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import whatis.love.agedate.api.model.UserAccessLevel
import whatis.love.agedate.chats.ui.ChatListPaywallScreen
import whatis.love.agedate.chats.ui.ChatListScreen
import whatis.love.agedate.chats.ui.ChatScreen
import whatis.love.agedate.chats.viewmodel.ChatListViewModel
import whatis.love.agedate.discover.ui.DiscoverScreen
import whatis.love.agedate.discover.viewmodel.DiscoverViewModel
import whatis.love.agedate.home.ui.HomeScreen
import whatis.love.agedate.likes.viewmodel.LikeViewModel
import whatis.love.agedate.profile.ui.MeScreen
import whatis.love.agedate.profile.ui.ProfileQRScreen
import whatis.love.agedate.profile.ui.ProfileScreen
import whatis.love.agedate.profile.viewmodel.ProfileQRScreenViewModel
import whatis.love.agedate.profile.viewmodel.ProfileScreenViewModel
import whatis.love.agedate.qrcode.ui.QRScannerScreen
import whatis.love.agedate.questionnaire.QuestionnaireScreen
import whatis.love.agedate.user.viewmodel.UserViewModel
import whatis.love.agedate.util.APP_SCHEME

@Composable
fun AuthenticatedNavHost(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier,
    topPaddingValues: PaddingValues,
) {
    val discoverViewModel: DiscoverViewModel = hiltViewModel()
    val likeViewModel: LikeViewModel = hiltViewModel()
    val chatListViewModel: ChatListViewModel = hiltViewModel()
    val user = userViewModel.userProfile.collectAsStateWithLifecycle()

    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .padding(topPaddingValues),
        bottomBar = {
            NavBar(navController)
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProfile = { profileID ->
                        navController.navigate(Screen.Profile.createRoute(profileID))
                    },
                    onNavigateToDiscover = {
                        navController.popBackStack()
                        navController.navigate(Screen.Discover.route)
                    },
                    onNavigateToQRScanner = {
                        navController.navigate(Screen.ProfileQRScanner.route)
                    },
                    navbarPaddingValues = paddingValues,
                )
            }
            composable(
                route = Screen.Profile.route,
                arguments =
                    listOf(
                        navArgument("profileID") { type = NavType.StringType },
                    ),
                deepLinks = Screen.Profile.getDeepLinks(),
            ) { backStackEntry ->
                val viewModel: ProfileScreenViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = viewModel,
                    userViewModel = userViewModel,
                    onBackClick = { navController.navigateUp() },
                    onShareClick = { profileID ->
                        navController.navigate(Screen.ProfileQRScreen.createRoute(profileID))
                    },
                    onMessageClick = {
                        navController.navigate(Screen.Chat.createRoute(it))
                    },
                )
            }

            composable(
                route = Screen.Discover.route,
            ) {
                DiscoverScreen(
                    discoverViewModel = discoverViewModel,
                    likeViewModel = likeViewModel,
                    onDetailsClick = { profileID: String ->
                        navController.navigate(Screen.Profile.createRoute(profileID))
                    },
                    navbarPaddingValues = paddingValues,
                )
            }

            composable(
                route = Screen.ChatList.route,
            ) {
                if (user.value?.accessLevel == UserAccessLevel.TRIAL) {
                    ChatListPaywallScreen(userViewModel, paddingValues)
                } else {
                    ChatListScreen(
                        viewModel = chatListViewModel,
                        onNavigateToChat = { profileID ->
                            navController.navigate(Screen.Chat.createRoute(profileID))
                        },
                    )
                }
            }

            composable(
                route = Screen.Chat.route,
                arguments =
                    listOf(
                        navArgument("profileID") { type = NavType.StringType },
                    ),
            ) {
                if (user.value?.accessLevel == UserAccessLevel.TRIAL) {
                    ChatListPaywallScreen(userViewModel, paddingValues)
                } else {
                    ChatScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                    )
                }
            }

            composable(
                route = Screen.ProfileQRScreen.route,
                arguments =
                    listOf(
                        navArgument("profileID") { type = NavType.StringType },
                    ),
            ) {
                val viewModel: ProfileQRScreenViewModel = hiltViewModel()
                ProfileQRScreen(
                    viewModel = viewModel,
                    appPackageName = "what.is.love.agedate",
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                )
            }

            composable(
                route = Screen.ProfileQRScanner.route,
            ) {
                QRScannerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    validCodePredicate = {
                        it.startsWith("$APP_SCHEME://profile/")
                    },
                    onQRCodeScanned = {
                        navController.navigate(it.removePrefix("$APP_SCHEME://")) {
                            popUpTo(
                                navController.currentBackStackEntry?.destination?.id
                                    ?: return@navigate,
                            ) {
                                inclusive = true
                            }
                        }
                    },
                )
            }

            composable(
                route = Screen.Me.route,
            ) { backStackEntry ->
                MeScreen(
                    viewModel = userViewModel,
                    onBackClick = { navController.navigateUp() },
                    onEditClick = { navController.navigate(Screen.Questionnaire.route) },
                    onShareClick = { profileID ->
                        navController.navigate(Screen.ProfileQRScreen.createRoute(profileID))
                    },
                )
            }

            composable(Screen.Questionnaire.route) {
                QuestionnaireScreen(
                    userViewModel = userViewModel,
                    populateFromViewModel = true,
                    onBack = { navController.navigateUp() },
                    onSuccess = { navController.navigateUp() },
                ) {
                    userViewModel.onboardingQuestionnaire(it)
                }
            }
        }
    }
}
