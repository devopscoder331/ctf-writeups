package app.blackhol3.data.remote.model

class ApiException(
    val code: Int,
    override val message: String,
) : Exception()
