package whatis.love.agedate.api.client

import kotlinx.serialization.json.Json
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class APIResponseCallAdapterFactory : CallAdapter.Factory() {
    private val json = Json { ignoreUnknownKeys = true }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        check(returnType is ParameterizedType) { "Return type must be parameterized as Call<ApiResponse<T>>" }

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != APIResponse::class.java) {
            return null
        }

        check(responseType is ParameterizedType) { "Response type must be parameterized as APIResponse<T>" }

        val successBodyType = getParameterUpperBound(0, responseType)
        val errorBodyConverter =
            retrofit.responseBodyConverter<Any>(
                APIErrorResponse::class.java,
                annotations,
            )
        val serverErrorBodyConverter =
            retrofit.responseBodyConverter<Any>(
                AppServerErrorResponse::class.java,
                annotations,
            )

        return APIResponseCallAdapter<Any>(
            successBodyType,
            errorBodyConverter,
            serverErrorBodyConverter,
            json,
        )
    }
}
