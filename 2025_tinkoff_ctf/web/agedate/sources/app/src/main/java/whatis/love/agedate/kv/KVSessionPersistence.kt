package whatis.love.agedate.kv

import whatis.love.agedate.api.client.session.SessionPersistence
import javax.inject.Inject

private const val KV_TYPE_SESSION = "session"
private const val KV_KEY_SESSION_TOKEN = "token"

class KVSessionPersistence
    @Inject
    constructor(
        val kv: KVStorage,
    ) : SessionPersistence {
        override fun getAuthorizationToken(): String? = kv.getString(KV_TYPE_SESSION, KV_KEY_SESSION_TOKEN)

        override fun setAuthorizationToken(token: String) {
            kv.setString(KV_TYPE_SESSION, KV_KEY_SESSION_TOKEN, token, ActionOnConflict.REPLACE)
        }
    }
