package whatis.love.agedate.api.client

import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Converter
import java.lang.reflect.Type

class APIResponseCallAdapter<T>(
    private val successType: Type,
    private val errorBodyConverter: Converter<ResponseBody, Any>,
    private val serverErrorBodyConverter: Converter<ResponseBody, Any>,
    private val json: Json,
) : CallAdapter<T, Call<APIResponse<T>>> {
    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<APIResponse<T>> = APIResponseCall(call, errorBodyConverter, serverErrorBodyConverter, json)
}
