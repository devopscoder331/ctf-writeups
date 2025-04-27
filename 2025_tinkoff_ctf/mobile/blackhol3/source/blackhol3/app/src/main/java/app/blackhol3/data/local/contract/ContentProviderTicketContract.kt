package app.blackhol3.data.local.contract

import android.content.ContentValues
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.model.ContentProviderTicket

object ContentProviderTicketContract {
    const val TABLE_NAME = "tickets"

    const val COLUMN_ID = "id"
    const val COLUMN_PRIVKEY_ID = "privkey_id"
    const val COLUMN_MEDIA_ID = "media_id"

    const val SQL_CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_PRIVKEY_ID TEXT NOT NULL,
            $COLUMN_MEDIA_ID TEXT NOT NULL,
            UNIQUE ($COLUMN_PRIVKEY_ID, $COLUMN_MEDIA_ID) ON CONFLICT REPLACE
        );
    """

    fun insertTicket(
        db: DatabaseHelper,
        ticket: ContentProviderTicket,
    ) {
        db.writableDatabase.let {
            it.insert(
                TABLE_NAME,
                null,
                ContentValues().apply {
                    put(COLUMN_ID, ticket.id)
                    put(COLUMN_PRIVKEY_ID, ticket.privKeyID)
                    put(COLUMN_MEDIA_ID, ticket.mediaID)
                },
            )
        }
    }

    fun getByID(
        db: DatabaseHelper,
        ticketID: String,
    ): ContentProviderTicket? =
        db.readableDatabase.let { db ->
            db
                .query(
                    TABLE_NAME,
                    null,
                    "$COLUMN_ID = ?",
                    arrayOf(ticketID),
                    null,
                    null,
                    null,
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        ContentProviderTicket(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIVKEY_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_ID)),
                        )
                    } else {
                        null
                    }
                }
        }
}
