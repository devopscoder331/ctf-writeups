package app.blackhol3.data.local.dao

import android.database.Cursor
import android.graphics.Bitmap
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.contract.PrivateKeyContract
import app.blackhol3.model.PrivateKey
import app.blackhol3.util.toBitmap
import app.blackhol3.util.toPNGBytes

class PrivateKeyDaoImpl(
    private val databaseHelper: DatabaseHelper,
) : PrivateKeyDao {
    private val bitmapCache = mutableMapOf<String, Bitmap>()

    private fun privateKeyFromCursor(cursor: Cursor): PrivateKey {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(PrivateKeyContract.COLUMN_ID))
        val keyPic =
            bitmapCache.getOrPut(id) {
                cursor
                    .getBlob(cursor.getColumnIndexOrThrow(PrivateKeyContract.COLUMN_KEYPIC))
                    .toBitmap()
            }

        return PrivateKey(
            cursor.getString(cursor.getColumnIndexOrThrow(PrivateKeyContract.COLUMN_ID)),
            cursor.getBlob(cursor.getColumnIndexOrThrow(PrivateKeyContract.COLUMN_PRIVATE_KEY)),
            keyPic,
        )
    }

    override fun listPrivateKeys(): List<PrivateKey> =
        PrivateKeyContract.listPrivateKeys(databaseHelper) { cursor ->
            privateKeyFromCursor(cursor)
        }

    override fun getPrivateKey(id: String): PrivateKey? {
        val privateKey =
            PrivateKeyContract.getPrivateKey(databaseHelper, id) { cursor ->
                privateKeyFromCursor(cursor)
            }
        return privateKey
    }

    override fun insertPrivateKey(privateKey: PrivateKey): Long {
        val pngBytes = privateKey.keyPic.toPNGBytes()
        val result =
            PrivateKeyContract.insertPrivateKey(
                databaseHelper,
                privateKey.id,
                privateKey.privateKeyBytes,
                pngBytes,
            )
        bitmapCache.put(privateKey.id, privateKey.keyPic)
        return result
    }

    override fun deletePrivateKey(id: String) {
        PrivateKeyContract.deletePrivateKey(databaseHelper, id)
        bitmapCache.remove(id)
    }
}
