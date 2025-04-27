package app.blackhol3.navigation

sealed class NavigationState {
    object Loading : NavigationState()

    object NoKey : NavigationState()

    object WithKey : NavigationState()
}
