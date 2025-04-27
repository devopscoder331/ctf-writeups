package app.blackhol3.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.blackhol3.ui.keygen.KeyGenerationScreen
import app.blackhol3.ui.welcome.WelcomeScreen

@Composable
fun NoKeyNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Intro.path,
    ) {
        composable(Screen.Intro.path) {
            WelcomeScreen {
                navController.navigate(Screen.KeyGen.path)
            }
        }

        composable(Screen.KeyGen.path) {
            KeyGenerationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onKeySaved = {},
            )
        }
    }
}
