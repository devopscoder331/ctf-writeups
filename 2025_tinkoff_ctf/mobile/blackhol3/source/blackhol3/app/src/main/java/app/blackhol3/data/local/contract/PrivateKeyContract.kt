package app.blackhol3.data.local.contract

import android.content.ContentValues
import android.database.Cursor
import app.blackhol3.data.local.DatabaseHelper
import io.requery.android.database.sqlite.SQLiteDatabase

object PrivateKeyContract {
    const val TABLE_NAME = "privkeys"

    const val COLUMN_ID = "id"
    const val COLUMN_PRIVATE_KEY = "private_key"
    const val COLUMN_KEYPIC = "keypic"

    const val SQL_CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_PRIVATE_KEY BLOB NOT NULL,
            $COLUMN_KEYPIC BLOB NOT NULL,
            UNIQUE($COLUMN_ID) ON CONFLICT REPLACE
        )
    """

    fun <T> listPrivateKeys(
        db: DatabaseHelper,
        converter: (Cursor) -> T,
    ): List<T> {
        db.readableDatabase.let { database ->
            database.query(TABLE_NAME, null, null, null, null, null, null).use { cursor ->
                val result = mutableListOf<T>()
                while (cursor.moveToNext()) {
                    result.add(converter(cursor))
                }
                return result
            }
        }
    }

    fun <T> getPrivateKey(
        db: DatabaseHelper,
        id: String,
        converter: (Cursor) -> T?,
    ): T? =
        db.readableDatabase.let { database ->
            database
                .query(
                    TABLE_NAME,
                    null,
                    "$COLUMN_ID = ?",
                    arrayOf(id),
                    null,
                    null,
                    null,
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        converter(cursor)
                    } else {
                        null
                    }
                }
        }

    fun insertPrivateKey(
        db: DatabaseHelper,
        id: String,
        privateKeyBytes: ByteArray,
        keyPic: ByteArray,
    ): Long =
        db.writableDatabase.let { database ->
            val values =
                ContentValues().apply {
                    put(COLUMN_ID, id)
                    put(COLUMN_PRIVATE_KEY, privateKeyBytes)
                    put(COLUMN_KEYPIC, keyPic)
                }
            database.insertWithOnConflict(
                TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE,
            )
        }

    fun deletePrivateKey(
        db: DatabaseHelper,
        id: String,
    ) {
        db.writableDatabase.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id),
        )
    }
}
