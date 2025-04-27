package app.blackhol3.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.blackhol3.ui.chat.ChatScreen
import app.blackhol3.ui.chat.ChatViewModel
import app.blackhol3.ui.chatlist.ChatListScreen
import app.blackhol3.ui.common.PrivateKeyViewModel
import app.blackhol3.ui.keygen.KeyGenerationScreen
import app.blackhol3.ui.pubkey.ImportPublicKeyScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WithKeyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val privateKeyViewModel: PrivateKeyViewModel = koinViewModel()
    NavHost(
        navController = navController,
        startDestination = Screen.ChatList.path,
        enterTransition = { slideInHorizontally { width -> width } + fadeIn() },
        exitTransition = { slideOutHorizontally { width -> -width } + fadeOut() },
        popEnterTransition = { slideInHorizontally { width -> -width } + fadeIn() },
        popExitTransition = { slideOutHorizontally { width -> width } + fadeOut() },
    ) {
        composable(Screen.ChatList.path) {
            ChatListScreen(
                onChatSelected = {
                    navController.navigate(Screen.Chat.createRoute(it))
                },
                onAddChatClicked = {
                    navController.navigate(Screen.ImportPubKey.path)
                },
                onNewKeyClicked = {
                    navController.navigate(Screen.KeyGen.path)
                },
                onKeyClicked = {
                    privateKeyViewModel.choosePrivateKey(it)
                },
                onRegenerateKeyClicked = {
                    navController.navigate(Screen.KeyRegen.createRoute(it))
                },
            )
        }

        composable(Screen.KeyGen.path) {
            KeyGenerationScreen(onNavigateBack = {
                navController.popBackStack()
            }, onKeySaved = {
                navController.popBackStack()
            })
        }

        composable(
            route = Screen.KeyRegen.path,
            arguments = listOf(navArgument("keyId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("keyId")!!
            KeyGenerationScreen(
                regeneratedKeyId = chatId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onKeySaved = {
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Screen.Chat.path,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val viewModel =
                koinViewModel<ChatViewModel> {
                    parametersOf(chatId)
                }
            ChatScreen(
                viewModel = viewModel,
                modifier = modifier,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(Screen.ImportPubKey.path) {
            ImportPublicKeyScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onAddKeySuccess = {
                    navController.popBackStack()
                },
            )
        }
    }
}
