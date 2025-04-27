package app.blackhol3.data.local.contract

import android.content.ContentValues
import android.database.Cursor
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.envelope.LocalMediaMetadataEnvelope
import io.requery.android.database.sqlite.SQLiteDatabase

object MediaContract {
    const val TABLE_NAME = "media"

    const val COLUMN_ID = "id"
    const val COLUMN_PRIVKEY_ID = "privkey_id"
    const val COLUMN_METADATA = "metadata"

    const val SQL_CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_PRIVKEY_ID TEXT NOT NULL,
            $COLUMN_METADATA BLOB NOT NULL,
            FOREIGN KEY ($COLUMN_PRIVKEY_ID) REFERENCES ${PrivateKeyContract.TABLE_NAME} (${PrivateKeyContract.COLUMN_ID})
        )
    """

    fun saveMedia(
        db: DatabaseHelper,
        mediaId: String,
        privateKeyId: String,
        encryptedMetadata: ByteArray,
    ): Long {
        val values =
            ContentValues().apply {
                put(COLUMN_ID, mediaId)
                put(COLUMN_PRIVKEY_ID, privateKeyId)
                put(COLUMN_METADATA, encryptedMetadata)
            }

        return db.writableDatabase.insertWithOnConflict(
            TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun loadMediaMetadata(
        db: DatabaseHelper,
        privKeyID: String,
        mediaId: String,
        transform: (Cursor) -> LocalMediaMetadataEnvelope?,
    ): LocalMediaMetadataEnvelope? =
        db.readableDatabase.let {
            it
                .query(
                    TABLE_NAME,
                    arrayOf(COLUMN_ID, COLUMN_METADATA),
                    "$COLUMN_ID = ? AND $COLUMN_PRIVKEY_ID = ?",
                    arrayOf(mediaId, privKeyID),
                    null,
                    null,
                    null,
                ).use {
                    if (it.moveToFirst()) {
                        transform(it)
                    } else {
                        null
                    }
                }
        }

    fun deleteMedia(
        db: DatabaseHelper,
        mediaId: String,
    ): Int =
        db.writableDatabase.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(mediaId),
        )
}
