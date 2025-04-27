package whatis.love.agedate.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import whatis.love.agedate.util.APP_SCHEME

fun NavGraphBuilder.redirectDeepLinksToRoot(navController: NavHostController) {
    composable(
        route = "deeplink/{path}",
        arguments =
            listOf(
                navArgument("path") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        deepLinks =
            listOf(
                navDeepLink { uriPattern = "$APP_SCHEME://{path}" },
            ),
    ) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
            navController.navigate(navController.graph.startDestinationRoute ?: "") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }
}
