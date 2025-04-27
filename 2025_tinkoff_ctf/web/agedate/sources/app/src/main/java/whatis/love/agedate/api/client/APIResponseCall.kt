package whatis.love.agedate.api.client

import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

class APIResponseCall<T>(
    private val delegate: Call<T>,
    private val errorBodyConverter: Converter<ResponseBody, Any>,
    private val serverErrorBodyConverter: Converter<ResponseBody, Any>,
    private val json: Json,
) : Call<APIResponse<T>> {
    override fun enqueue(callback: Callback<APIResponse<T>>) {
        delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(
                    call: Call<T>,
                    response: Response<T>,
                ) {
                    val apiResponse =
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                APIResponse.Success(body)
                            } else {
                                APIResponse.UnknownError("Response body is null", response.code())
                            }
                        } else {
                            val errorBody = response.errorBody()
                            if (errorBody != null) {
                                try {
                                    when {
                                        response.code() in 400..499 -> {
                                            val errorResponse =
                                                parseErrorBody<APIErrorResponse>(errorBody.string())
                                            if (errorResponse != null) {
                                                APIResponse.APIError(errorResponse)
                                            } else {
                                                val serverErrorResponse =
                                                    parseErrorBody<AppServerErrorResponse>(errorBody.string())
                                                if (serverErrorResponse != null) {
                                                    APIResponse.ServerError(serverErrorResponse)
                                                } else {
                                                    APIResponse.UnknownError(
                                                        errorBody.string(),
                                                        response.code(),
                                                    )
                                                }
                                            }
                                        }

                                        response.code() in 500..599 -> {
                                            val serverErrorResponse =
                                                parseErrorBody<AppServerErrorResponse>(errorBody.string())
                                            if (serverErrorResponse != null) {
                                                APIResponse.ServerError(serverErrorResponse)
                                            } else {
                                                APIResponse.UnknownError(
                                                    errorBody.string(),
                                                    response.code(),
                                                )
                                            }
                                        }

                                        else ->
                                            APIResponse.UnknownError(
                                                errorBody.string(),
                                                response.code(),
                                            )
                                    }
                                } catch (e: Exception) {
                                    APIResponse.UnknownError(errorBody.string(), response.code())
                                }
                            } else {
                                APIResponse.UnknownError(null, response.code())
                            }
                        }
                    callback.onResponse(this@APIResponseCall, Response.success(apiResponse))
                }

                override fun onFailure(
                    call: Call<T>,
                    t: Throwable,
                ) {
                    val apiResponse = APIResponse.NetworkError(t)
                    callback.onResponse(this@APIResponseCall, Response.success(apiResponse))
                }
            },
        )
    }

    private inline fun <reified T> parseErrorBody(errorBody: String): T? =
        try {
            json.decodeFromString<T>(errorBody)
        } catch (e: Exception) {
            null
        }

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun execute(): Response<APIResponse<T>> = throw UnsupportedOperationException("APIResponseCall doesn't support execute")

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun clone(): Call<APIResponse<T>> = APIResponseCall(delegate.clone(), errorBodyConverter, serverErrorBodyConverter, json)

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}
