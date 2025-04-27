package whatis.love.agedate.navigation

sealed class NavigationAuthState {
    object Onboarding : NavigationAuthState()

    object Authenticated : NavigationAuthState()

    object Unauthenticated : NavigationAuthState()
}
