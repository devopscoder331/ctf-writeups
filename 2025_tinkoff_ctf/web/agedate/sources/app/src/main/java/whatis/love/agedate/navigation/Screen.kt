package whatis.love.agedate.navigation

import androidx.navigation.NavDeepLink
import androidx.navigation.navDeepLink
import whatis.love.agedate.util.APP_SCHEME

sealed class Screen(
    val route: String,
    val deepLinkable: Boolean = false,
) {
    object Login : Screen("login")

    object Register : Screen("register")

    object Questionnaire : Screen("questionnaire")

    object Home : Screen("home")

    object Discover : Screen("discover")

    object Profile : Screen("profile/{profileID}", true) {
        fun createRoute(profileID: String) = "profile/$profileID"
    }

    object ChatList : Screen("chats/{profileID}", false)

    object Me : Screen("me")

    object Chat : Screen("chats/{profileID}", false) {
        fun createRoute(profileID: String) = "chats/$profileID"
    }

    object ProfileQRScanner : Screen("profile_qr_scanner", true)

    object ProfileQRScreen : Screen("profile_qr_screen/{profileID}") {
        fun createRoute(profileID: String) = "profile_qr_screen/$profileID"
    }

    fun getDeepLinks(): List<NavDeepLink> =
        if (deepLinkable) {
            listOf(
                navDeepLink { uriPattern = "$APP_SCHEME://$route" },
            )
        } else {
            emptyList()
        }
}
