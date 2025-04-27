package whatis.love.agedate.api.client

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.client.pinning.PinningParametersRepo

fun AgeDateClient(
    client: OkHttpClient,
    pinningParametersRepo: PinningParametersRepo,
): AgeDateAPI =
    Retrofit
        .Builder()
        .client(client)
        .baseUrl(pinningParametersRepo.getHost())
        .addConverterFactory(
            Json.asConverterFactory(
                "application/json; charset=UTF8".toMediaType(),
            ),
        ).addCallAdapterFactory(APIResponseCallAdapterFactory())
        .build()
        .create<AgeDateAPI>(AgeDateAPI::class.java)
