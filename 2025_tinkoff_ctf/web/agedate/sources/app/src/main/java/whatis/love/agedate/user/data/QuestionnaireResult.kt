package whatis.love.agedate.user.data

sealed class QuestionnaireResult<out T> {
    object Initial : QuestionnaireResult<Nothing>()

    data class Success<T>(
        val data: T,
    ) : QuestionnaireResult<T>()

    data class Error(
        val message: String?,
        val firstNameError: String? = null,
        val lastNameError: String? = null,
        val dateError: String? = null,
        val bioError: String? = null,
    ) : QuestionnaireResult<Nothing>()

    object Loading : QuestionnaireResult<Nothing>()
}
