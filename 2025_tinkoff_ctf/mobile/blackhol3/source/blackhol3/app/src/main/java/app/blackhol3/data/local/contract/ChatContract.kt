package app.blackhol3.data.local.contract

import android.content.ContentValues
import android.database.Cursor
import app.blackhol3.data.local.DatabaseHelper

object ChatContract {
    const val TABLE_NAME = "chats"

    const val COLUMN_ID = "id"
    const val COLUMN_SEQ = "seq"
    const val COLUMN_PRIVKEY_ID = "privkey_id"
    const val COLUMN_NAME = "name"
    const val COLUMN_PUBKEY = "pubkey"
    const val COLUMN_PUBKEY_FINGERPRINT = "pubkey_fingerprint"
    const val COLUMN_PUBKEY_KEYPIC = "pubkey_keypic"

    const val SQL_CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_SEQ INTEGER NOT NULL DEFAULT 0,
            $COLUMN_PRIVKEY_ID TEXT NOT NULL,
            $COLUMN_NAME BLOB NOT NULL,
            $COLUMN_PUBKEY BLOB NOT NULL,
            $COLUMN_PUBKEY_FINGERPRINT TEXT NOT NULL,
            $COLUMN_PUBKEY_KEYPIC BLOB NOT NULL,
            UNIQUE ($COLUMN_PRIVKEY_ID, $COLUMN_PUBKEY_FINGERPRINT) ON CONFLICT REPLACE
        );
    """

    const val SQL_GET_CHATS = """
        WITH last_message AS (
            SELECT ${MessageContract.COLUMN_CHAT_ID} AS id, MAX(${MessageContract.COLUMN_CHAT_SEQ}) AS seq
            FROM ${MessageContract.TABLE_NAME}
        )
        SELECT c.* FROM $TABLE_NAME c
        LEFT JOIN last_message ON c.$COLUMN_ID = last_message.id 
        WHERE $COLUMN_PRIVKEY_ID = ? 
        ORDER BY last_message.seq DESC, c.$COLUMN_SEQ DESC, $COLUMN_ID ASC
    """

    fun <T> getChats(
        db: DatabaseHelper,
        privKeyId: String,
        converter: (Cursor) -> T?,
    ): List<T> {
        db.readableDatabase.let { db ->
            db.rawQuery(SQL_GET_CHATS, arrayOf(privKeyId)).use {
                val result = mutableListOf<T>()
                while (it.moveToNext()) {
                    converter(it)?.let(result::add)
                }
                return result
            }
        }
    }

    fun <T> getById(
        db: DatabaseHelper,
        privKeyId: String,
        chatId: String,
        converter: (Cursor) -> T?,
    ): T? =
        db.readableDatabase.let { db ->
            db
                .query(
                    TABLE_NAME,
                    null,
                    "$COLUMN_PRIVKEY_ID = ? AND $COLUMN_ID = ?",
                    arrayOf(privKeyId, chatId),
                    null,
                    null,
                    null,
                ).use {
                    if (it.moveToFirst()) {
                        converter(it)
                    } else {
                        null
                    }
                }
        }

    fun <T> getByPubKey(
        db: DatabaseHelper,
        privKeyId: String,
        fingerprint: String,
        converter: (Cursor) -> T?,
    ): T? =
        db.readableDatabase.let { db ->
            db
                .query(
                    TABLE_NAME,
                    null,
                    "$COLUMN_PRIVKEY_ID = ? AND $COLUMN_PUBKEY_FINGERPRINT = ?",
                    arrayOf(privKeyId, fingerprint),
                    null,
                    null,
                    null,
                ).use {
                    if (it.moveToFirst()) {
                        converter(it)
                    } else {
                        null
                    }
                }
        }

    const val SQL_INSERT_CHAT = """
        INSERT INTO $TABLE_NAME ($COLUMN_ID, $COLUMN_SEQ, $COLUMN_PRIVKEY_ID, $COLUMN_NAME, $COLUMN_PUBKEY, $COLUMN_PUBKEY_FINGERPRINT, $COLUMN_PUBKEY_KEYPIC)
        VALUES (
            ?, 
            (SELECT COALESCE(MAX($COLUMN_SEQ), 0) + 1 FROM $TABLE_NAME WHERE $COLUMN_PRIVKEY_ID = ?), 
            ?, 
            ?, 
            ?, 
            ?,
            ?
        )
    """

    fun addChat(
        db: DatabaseHelper,
        id: String,
        privKeyId: String,
        name: ByteArray?,
        pubKey: ByteArray,
        pubKeyFingerprint: String,
        pubKeyPic: ByteArray,
    ) {
        db.writableDatabase.execSQL(
            SQL_INSERT_CHAT,
            arrayOf(id, privKeyId, privKeyId, name, pubKey, pubKeyFingerprint, pubKeyPic),
        )
    }

    fun renameChat(
        db: DatabaseHelper,
        id: String,
        name: ByteArray?,
    ) {
        db.writableDatabase.let { db ->
            db.update(
                TABLE_NAME,
                ContentValues().apply {
                    put(COLUMN_NAME, name)
                },
                "$COLUMN_ID = ?",
                arrayOf(id),
            )
        }
    }

    fun deleteChat(
        db: DatabaseHelper,
        privKeyId: String,
        chatId: String,
    ) {
        db.writableDatabase.delete(
            TABLE_NAME,
            "$COLUMN_PRIVKEY_ID = ? AND $COLUMN_ID = ?",
            arrayOf(privKeyId, chatId),
        )
    }
}
