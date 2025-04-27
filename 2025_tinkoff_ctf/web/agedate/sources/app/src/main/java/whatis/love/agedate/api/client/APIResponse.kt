package whatis.love.agedate.api.client

const val PLEASE_TRY_AGAIN_LATER = "Пожалуйста, попробуйте позже"

sealed class APIResponse<out T> {
    data class Success<T>(
        val data: T,
    ) : APIResponse<T>()

    data class APIError(
        val data: APIErrorResponse,
    ) : APIResponse<Nothing>()

    data class ServerError(
        val body: AppServerErrorResponse,
    ) : APIResponse<Nothing>()

    data class NetworkError(
        val throwable: Throwable,
    ) : APIResponse<Nothing>()

    data class UnknownError(
        val errorBody: String?,
        val httpCode: Int?,
    ) : APIResponse<Nothing>()

    fun toResult(): Result<T> =
        when (this) {
            is Success -> Result.success(data)
            is APIError ->
                Result.failure(
                    APIFetchFailedException(
                        "Ошибка ${data.status}",
                        data.error,
                    ),
                )

            is ServerError ->
                Result.failure(
                    throw APIFetchFailedException(
                        "Ошибка ${body.status}",
                        body.error,
                    ),
                )

            is NetworkError ->
                Result.failure(
                    throw APIFetchFailedException(
                        "Ошибка сети",
                        throwable.message ?: PLEASE_TRY_AGAIN_LATER,
                    ),
                )

            is UnknownError ->
                Result.failure(
                    throw APIFetchFailedException(
                        "Неизвестная ошибка",
                        errorBody ?: PLEASE_TRY_AGAIN_LATER,
                    ),
                )
        }
}
