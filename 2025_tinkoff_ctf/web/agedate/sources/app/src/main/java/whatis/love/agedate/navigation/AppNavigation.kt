package whatis.love.agedate.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import whatis.love.agedate.user.viewmodel.UserState
import whatis.love.agedate.user.viewmodel.UserViewModel

@Composable
fun AppNavigation(
    userViewModel: UserViewModel,
    topPaddingValues: PaddingValues = PaddingValues(),
) {
    val unauthenticatedNavController = rememberNavController()
    val onboardingNavController = rememberNavController()
    val authenticatedNavController = rememberNavController()

    val authState by userViewModel.userState.collectAsState()
    val authNavigationState =
        when (authState) {
            is UserState.Onboarding -> NavigationAuthState.Onboarding
            is UserState.Authenticated -> NavigationAuthState.Authenticated
            else -> NavigationAuthState.Unauthenticated
        }
    Surface(
        modifier =
            Modifier
                .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(
            targetState = authNavigationState,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                    (slideOutHorizontally { width -> -width } + fadeOut())
            },
            modifier = Modifier.fillMaxSize(),
        ) { currentAuthState ->
            when (currentAuthState) {
                is NavigationAuthState.Onboarding ->
                    OnboardingNavHost(
                        navController = unauthenticatedNavController,
                        userViewModel = userViewModel,
                    )

                is NavigationAuthState.Authenticated ->
                    AuthenticatedNavHost(
                        navController = authenticatedNavController,
                        userViewModel = userViewModel,
                        topPaddingValues = topPaddingValues,
                    )

                else -> {
                    UnauthenticatedNavHost(
                        navController = onboardingNavController,
                        userViewModel = userViewModel,
                    )
                }
            }
        }
    }
}
