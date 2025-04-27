package whatis.love.agedate.user.data

sealed class LoginResult<out T> {
    data class Success<T>(
        val data: T,
    ) : LoginResult<T>()

    data class Error(
        val message: String?,
        val usernameError: String? = null,
        val passwordError: String? = null,
    ) : LoginResult<Nothing>()

    object Loading : LoginResult<Nothing>()
}
