package app.blackhol3.data.local.contract

import android.content.ContentValues
import android.database.Cursor
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.model.DeliveryStatus

object MessageContract {
    const val TABLE_NAME = "messages"

    const val COLUMN_ID = "id"
    const val COLUMN_CHAT_ID = "chat_id"
    const val COLUMN_CHAT_SEQ = "chat_seq"
    const val COLUMN_GLOBAL_SEQ = "global_seq"
    const val COLUMN_CONTENT = "json_content"
    const val COLUMN_DELIVERY_STATUS = "delivery_status"

    const val SQL_CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_CHAT_ID TEXT NOT NULL,
            $COLUMN_CHAT_SEQ INTEGER NOT NULL,
            $COLUMN_GLOBAL_SEQ INTEGER NOT NULL,
            $COLUMN_CONTENT BLOB NOT NULL,
            $COLUMN_DELIVERY_STATUS TEXT NOT NULL,
            FOREIGN KEY ($COLUMN_CHAT_ID) REFERENCES ${ChatContract.TABLE_NAME} (${ChatContract.COLUMN_ID}) ON DELETE CASCADE
        )
    """

    const val SQL_INSERT_MESSAGE = """
        INSERT INTO $TABLE_NAME (
            $COLUMN_ID, 
            $COLUMN_CHAT_ID, 
            $COLUMN_CHAT_SEQ, 
            $COLUMN_GLOBAL_SEQ,
            $COLUMN_CONTENT, 
            $COLUMN_DELIVERY_STATUS
        ) VALUES (
            ?, 
            ?, 
            (SELECT COALESCE(MAX($COLUMN_CHAT_SEQ), 0) + 10 FROM $TABLE_NAME WHERE $COLUMN_CHAT_ID = ?),
            (SELECT COALESCE(MAX($COLUMN_GLOBAL_SEQ), 0) + 10 FROM $TABLE_NAME),
            ?, 
            ?
        ) RETURNING $COLUMN_CHAT_SEQ;
    """

    fun saveMessageEnvelope(
        db: DatabaseHelper,
        id: String,
        chatId: String,
        encryptedEnvelope: ByteArray,
        deliveryStatus: DeliveryStatus,
    ): Long =
        db.writableDatabase.let { db ->
            db
                .rawQuery(
                    SQL_INSERT_MESSAGE,
                    arrayOf(id, chatId, chatId, encryptedEnvelope, deliveryStatus.name),
                ).use {
                    it.moveToFirst()
                    it.getLong(0)
                }
        }

    fun updateMessageStatus(
        db: DatabaseHelper,
        messageId: String,
        status: DeliveryStatus,
    ) {
        db.writableDatabase.let { db ->
            val values =
                ContentValues().apply {
                    put(COLUMN_DELIVERY_STATUS, status.name)
                }
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(messageId))
        }
    }

    fun <T> loadMessages(
        db: DatabaseHelper,
        chatId: String,
        offset: Int,
        limit: Int,
        converter: (Cursor) -> T?,
    ): List<T> =
        db.readableDatabase.let { db ->
            db
                .query(
                    TABLE_NAME,
                    arrayOf(
                        COLUMN_ID,
                        COLUMN_CHAT_ID,
                        COLUMN_CHAT_SEQ,
                        COLUMN_CONTENT,
                        COLUMN_DELIVERY_STATUS,
                    ),
                    "$COLUMN_CHAT_ID = ?",
                    arrayOf(chatId),
                    null,
                    null,
                    "$COLUMN_CHAT_SEQ DESC",
                    "$limit, $offset",
                ).use { cursor ->
                    val result = mutableListOf<T>()
                    while (cursor.moveToNext()) {
                        converter(cursor)?.let(result::add)
                    }
                    result
                }
        }

    fun <T> loadAll(
        db: DatabaseHelper,
        chatId: String,
        converter: (Cursor) -> T?,
    ): List<T> =
        db.readableDatabase.let { db ->
            db
                .query(
                    TABLE_NAME,
                    arrayOf(
                        COLUMN_ID,
                        COLUMN_CHAT_ID,
                        COLUMN_CHAT_SEQ,
                        COLUMN_CONTENT,
                        COLUMN_DELIVERY_STATUS,
                    ),
                    "$COLUMN_CHAT_ID = ?",
                    arrayOf(chatId),
                    null,
                    null,
                    "$COLUMN_CHAT_SEQ ASC",
                ).use { cursor ->
                    val result = mutableListOf<T>()
                    while (cursor.moveToNext()) {
                        converter(cursor)?.let(result::add)
                    }
                    result
                }
        }

    fun <T> getById(
        db: DatabaseHelper,
        messageId: String,
        converter: (Cursor) -> T?,
    ): T? =
        db.readableDatabase.let { db ->
            db
                .query(
                    TABLE_NAME,
                    arrayOf(
                        COLUMN_ID,
                        COLUMN_CHAT_ID,
                        COLUMN_CHAT_SEQ,
                        COLUMN_CONTENT,
                        COLUMN_DELIVERY_STATUS,
                    ),
                    "$COLUMN_ID = ?",
                    arrayOf(messageId),
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
}
