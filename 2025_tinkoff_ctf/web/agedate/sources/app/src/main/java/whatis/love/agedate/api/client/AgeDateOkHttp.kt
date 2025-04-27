package whatis.love.agedate.api.client

import okhttp3.OkHttpClient
import whatis.love.agedate.api.client.pinning.PinningParametersRepo
import whatis.love.agedate.api.client.pinning.agedatePinning
import whatis.love.agedate.api.client.session.SessionInterceptor
import whatis.love.agedate.api.client.session.SessionPersistence

fun AgeDateOkHttp(
    pinningParametersRepo: PinningParametersRepo,
    sessionPersistence: SessionPersistence,
): OkHttpClient =
    OkHttpClient
        .Builder()
        .addInterceptor { req ->
            req.proceed(
                req
                    .request()
                    .newBuilder()
                    .header("User-Agent", "AgeDateClient/1.0.0")
                    .build(),
            )
        }.agedatePinning(pinningParametersRepo)
        .addInterceptor(SessionInterceptor(sessionPersistence))
        .followRedirects(false)
        .build()
