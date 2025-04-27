package whatis.love.agedate.user.viewmodel

sealed class UserState {
    object Initial : UserState()

    object Loading : UserState()

    object Onboarding : UserState()

    object Authenticated : UserState()

    object Unauthenticated : UserState()

    data class Error(
        val message: String? = null,
        val usernameError: String? = null,
        val passwordError: String? = null,
    ) : UserState()
}
