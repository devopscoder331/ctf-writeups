package whatis.love.agedate.api.client.session

interface SessionPersistence {
    fun getAuthorizationToken(): String?

    fun setAuthorizationToken(token: String)
}
