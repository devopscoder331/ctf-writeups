package whatis.love.agedate.kv

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteBackedKVStorage(
    context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION),
    KVStorage {
    companion object {
        private const val DATABASE_NAME = "kv.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_KV = "kv"
        private const val COLUMN_KEY = "key"
        private const val COLUMN_VALUE = "value"
        private const val COLUMN_TIMESTAMP = "ts"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_KV (
                $COLUMN_KEY TEXT PRIMARY KEY,
                $COLUMN_VALUE BLOB NOT NULL,
                $COLUMN_TIMESTAMP INTEGER DEFAULT CURRENT_TIMESTAMP
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX idx_key ON $TABLE_KV ($COLUMN_KEY)")
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int,
    ) {
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun setBytes(
        type: String,
        key: String,
        value: ByteArray,
        actionOnConflict: ActionOnConflict,
    ) {
        val dbKey = "$type:$key"
        val query =
            "INSERT OR ${actionOnConflict.name} INTO $TABLE_KV ($COLUMN_KEY, $COLUMN_VALUE) VALUES ('$dbKey', X'${value.toHexString()}');"
        writableDatabase.execSQL(query)
    }

    override fun getBytes(
        type: String,
        key: String,
    ): ByteArray? = getBytes(type, key, 0)

    override fun getBytes(
        type: String,
        key: String,
        ttl: Long,
    ): ByteArray? {
        val dbKey = "$type:$key"
        var query =
            "SELECT $COLUMN_VALUE, $COLUMN_TIMESTAMP FROM $TABLE_KV WHERE $COLUMN_KEY = '$dbKey'"
        if (ttl > 0) {
            query += " AND $COLUMN_TIMESTAMP >= CURRENT_TIMESTAMP - $ttl"
        }
        readableDatabase.rawQuery(query, null).use {
            return if (it.moveToFirst()) {
                it.getBlob(0)
            } else {
                null
            }
        }
    }

    override fun getString(
        type: String,
        key: String,
    ): String? = getString(type, key, 0)

    override fun getString(
        type: String,
        key: String,
        ttl: Long,
    ): String? = getBytes(type, key, ttl)?.toString(Charsets.UTF_8)

    override fun setString(
        type: String,
        key: String,
        value: String,
        actionOnConflict: ActionOnConflict,
    ) {
        setBytes(type, key, value.toByteArray(Charsets.UTF_8), actionOnConflict)
    }

    override fun getLong(
        type: String,
        key: String,
    ): Long? = getLong(type, key, 0)

    override fun getLong(
        type: String,
        key: String,
        ttl: Long,
    ): Long? {
        val str = getString(type, key, ttl)
        return str?.toLongOrNull()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun setLong(
        type: String,
        key: String,
        value: Long,
        actionOnConflict: ActionOnConflict,
    ) {
        setString(type, key, value.toHexString(), actionOnConflict)
    }

    override fun delete(
        type: String,
        key: String,
    ) {
        writableDatabase.execSQL("DELETE FROM $TABLE_KV WHERE $COLUMN_KEY = '$type:$key'")
    }
}
