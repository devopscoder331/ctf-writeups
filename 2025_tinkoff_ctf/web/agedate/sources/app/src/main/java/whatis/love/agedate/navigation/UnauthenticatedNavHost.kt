package whatis.love.agedate.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import whatis.love.agedate.user.ui.LoginScreen
import whatis.love.agedate.user.ui.RegistrationScreen
import whatis.love.agedate.user.viewmodel.UserViewModel

@Composable
fun UnauthenticatedNavHost(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        enterTransition = {
            (slideInHorizontally { width -> width } + fadeIn())
        },
        exitTransition = {
            (slideOutHorizontally { width -> -width } + fadeOut())
        },
        modifier = modifier,
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                userViewModel = userViewModel,
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                },
            )
        }

        composable(Screen.Register.route) {
            RegistrationScreen(
                userViewModel = userViewModel,
                onBack = {
                    navController.navigateUp()
                },
                onRegisterSuccess = {
                },
            )
        }
        redirectDeepLinksToRoot(navController)
    }
}
