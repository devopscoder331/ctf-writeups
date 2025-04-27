package app.blackhol3.data.local

import android.content.Context
import app.blackhol3.data.local.contract.ChatContract
import app.blackhol3.data.local.contract.ContentProviderTicketContract
import app.blackhol3.data.local.contract.MediaContract
import app.blackhol3.data.local.contract.MessageContract
import app.blackhol3.data.local.contract.PrivateKeyContract
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(
    context: Context,
) : SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        DATABASE_VERSION,
    ) {
    companion object {
        const val DATABASE_NAME = "blackhole.db"
        const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.apply {
            pageSize = 4096
            execSQL("COMMIT;")
            execSQL("VACUUM;")
            execSQL("BEGIN TRANSACTION;")
            execSQL(MediaContract.SQL_CREATE_TABLE)
            execSQL(ChatContract.SQL_CREATE_TABLE)
            execSQL(MessageContract.SQL_CREATE_TABLE)
            execSQL(PrivateKeyContract.SQL_CREATE_TABLE)
            execSQL(ContentProviderTicketContract.SQL_CREATE_TABLE)
        }
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {}

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
        db.disableWriteAheadLogging()
        db.pageSize = 4096
        db.execSQL("PRAGMA auto_vacuum = 2;")
    }
}
