package whatis.love.agedate.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import whatis.love.agedate.questionnaire.QuestionnaireScreen
import whatis.love.agedate.user.viewmodel.UserViewModel

@Composable
fun OnboardingNavHost(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Questionnaire.route,
        modifier = modifier,
    ) {
        composable(Screen.Questionnaire.route) {
            QuestionnaireScreen(
                userViewModel = userViewModel,
                isFirstScreenBackable = false,
            ) {
                userViewModel.onboardingQuestionnaire(it)
            }
        }
        redirectDeepLinksToRoot(navController)
    }
}
