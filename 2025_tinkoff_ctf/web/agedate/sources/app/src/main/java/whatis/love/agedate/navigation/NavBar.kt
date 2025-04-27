package whatis.love.agedate.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

data class NavBarItem(
    val screen: Screen,
    val title: String,
    val icon: @Composable () -> Unit,
)

fun Screen.toNavBarItem(
    title: String,
    icon: @Composable () -> Unit,
) = NavBarItem(this, title, icon)

val navBarItems =
    mapOf(
        Screen.Home to
            Screen.Home.toNavBarItem("Главная") {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = null,
                )
            },
        Screen.Discover to
            Screen.Discover.toNavBarItem("Картотека") {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    contentDescription = null,
                )
            },
        Screen.ChatList to
            Screen.ChatList.toNavBarItem("Сообщения") {
                Icon(
                    Icons.Filled.MailOutline,
                    contentDescription = null,
                )
            },
        Screen.Me to
            Screen.Me.toNavBarItem("Вы") {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                )
            },
    )

val navBarRoutes = navBarItems.keys.map { it.route }

@Composable
fun NavBar(navController: NavController) {
    val currentRoute =
        navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route
    val shouldShowBottomNav = navBarRoutes.contains(currentRoute)

    AnimatedVisibility(
        visible = shouldShowBottomNav,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        NavigationBar {
            navBarItems.values.forEach { item ->
                val selected = currentRoute == item.screen.route

                NavigationBarItem(
                    icon = {
                        if (item.screen == Screen.ChatList) {
                            BadgedBox(badge = { Badge { Text("1") } }) {
                                item.icon()
                            }
                        } else {
                            item.icon()
                        }
                    },
                    label = { Text(item.title) },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}
