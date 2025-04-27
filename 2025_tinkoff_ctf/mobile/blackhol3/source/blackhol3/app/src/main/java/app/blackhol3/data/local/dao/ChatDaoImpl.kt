package app.blackhol3.data.local.dao

import android.database.Cursor
import android.graphics.Bitmap
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.contract.ChatContract
import app.blackhol3.data.local.model.Chat
import app.blackhol3.model.PrivateKey
import app.blackhol3.model.PublicKey
import app.blackhol3.service.EncryptionService
import app.blackhol3.util.toBitmap
import app.blackhol3.util.toPNGBytes

class ChatDaoImpl(
    val db: DatabaseHelper,
    val encryptionService: EncryptionService,
) : ChatDao {
    private val bitmapCache = mutableMapOf<String, Bitmap>()

    private fun chatFromCursor(
        privateKey: PrivateKey,
        cursor: Cursor,
    ): Chat? {
        val name: String
        try {
            name =
                encryptionService.decryptString(
                    privateKey,
                    cursor.getBlob(cursor.getColumnIndexOrThrow(ChatContract.COLUMN_NAME)),
                )
        } catch (e: Exception) {
            return null
        }

        val keyPic =
            bitmapCache.getOrPut(
                cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.COLUMN_ID)),
            ) {
                cursor
                    .getBlob(cursor.getColumnIndexOrThrow(ChatContract.COLUMN_PUBKEY_KEYPIC))
                    .toBitmap()
            }

        return Chat(
            id = cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.COLUMN_ID)),
            privKeyId = cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.COLUMN_PRIVKEY_ID)),
            name = name,
            pubKey =
                PublicKey(
                    cursor.getBlob(cursor.getColumnIndexOrThrow(ChatContract.COLUMN_PUBKEY)),
                    keyPic,
                ),
        )
    }

    override fun getChats(privateKey: PrivateKey): List<Chat> =
        ChatContract.getChats(db, privateKey.id) {
            chatFromCursor(privateKey, it)
        }

    override fun getById(
        privateKey: PrivateKey,
        chatId: String,
    ): Chat? =
        ChatContract.getById(db, privateKey.id, chatId) { cursor ->
            chatFromCursor(privateKey, cursor)
        }

    override fun getByPubKey(
        privateKey: PrivateKey,
        fingerprint: String,
    ): Chat? =
        ChatContract.getByPubKey(db, privateKey.id, fingerprint) {
            chatFromCursor(privateKey, it)
        }

    override fun addChat(
        privateKey: PrivateKey,
        chat: Chat,
    ) {
        ChatContract.addChat(
            db,
            chat.id,
            privateKey.id,
            chat.name?.let { encryptionService.encryptString(privateKey, it) },
            chat.pubKey.keyBytes,
            chat.pubKey.fingerprint,
            chat.pubKey.keyPic.toPNGBytes(),
        )
    }

    override fun renameChat(
        privateKey: PrivateKey,
        chatId: String,
        name: String,
    ) {
        ChatContract.renameChat(
            db,
            chatId,
            name.let { encryptionService.encryptString(privateKey, it) },
        )
    }

    override fun deleteChat(
        privKeyId: String,
        chatId: String,
    ) {
        ChatContract.deleteChat(db, privKeyId, chatId)
        bitmapCache.remove(chatId)
    }
}
