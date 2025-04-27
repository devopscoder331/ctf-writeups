package whatis.love.agedate.kv

enum class ActionOnConflict {
    ROLLBACK,
    ABORT,
    FAIL,
    IGNORE,
    REPLACE,
}

interface KVStorage {
    fun getString(
        type: String,
        key: String,
    ): String?

    fun getString(
        type: String,
        key: String,
        ttl: Long,
    ): String?

    fun setString(
        type: String,
        key: String,
        value: String,
        actionOnConflict: ActionOnConflict,
    )

    fun getLong(
        type: String,
        key: String,
    ): Long?

    fun getLong(
        type: String,
        key: String,
        ttl: Long,
    ): Long?

    fun setLong(
        type: String,
        key: String,
        value: Long,
        actionOnConflict: ActionOnConflict,
    )

    fun getBytes(
        type: String,
        key: String,
    ): ByteArray?

    fun getBytes(
        type: String,
        key: String,
        ttl: Long,
    ): ByteArray?

    fun setBytes(
        type: String,
        key: String,
        value: ByteArray,
        actionOnConflict: ActionOnConflict,
    )

    fun delete(
        type: String,
        key: String,
    )
}
