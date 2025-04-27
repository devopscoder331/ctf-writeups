package app.blackhol3.data.local.dao

interface ConfigDao {
    fun getString(
        key: String,
        defaultValue: String? = null,
    ): String?

    fun putString(
        key: String,
        value: String?,
    )

    fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long

    fun putLong(
        key: String,
        value: Long,
    )

    fun remove(key: String)
}
