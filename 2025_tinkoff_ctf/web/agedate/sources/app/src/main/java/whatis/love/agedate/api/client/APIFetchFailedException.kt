package whatis.love.agedate.api.client

class APIFetchFailedException(
    val header: String,
    message: String,
) : RuntimeException(message)
