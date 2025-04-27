package whatis.love.agedate.api.client.session

import okhttp3.Interceptor
import okhttp3.Response

class SessionInterceptor(
    private val sessionPersistence: SessionPersistence,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionPersistence.getAuthorizationToken()

        return if (!token.isNullOrEmpty()) {
            val newRequest =
                originalRequest
                    .newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
