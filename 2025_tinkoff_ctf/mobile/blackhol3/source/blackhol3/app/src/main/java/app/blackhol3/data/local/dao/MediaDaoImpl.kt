package app.blackhol3.data.local.dao

import android.content.Context
import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.contract.MediaContract
import app.blackhol3.data.local.envelope.LocalMediaMetadataEnvelope
import app.blackhol3.model.Media
import app.blackhol3.model.PrivateKey
import app.blackhol3.service.EncryptionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaDaoImpl(
    private val db: DatabaseHelper,
    private val encryptionService: EncryptionService,
    private val context: Context,
) : MediaDao {
    private val mediaDir by lazy {
        File(context.filesDir, "encrypted_media").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun saveMedia(
        privateKey: PrivateKey,
        media: Media,
    ): Long {
        val envelope =
            LocalMediaMetadataEnvelope(
                mime = media.mimeType,
                size = media.sizeBytes,
            )
        val encryptedMetadata = envelope.encrypt(encryptionService, privateKey)

        return withContext(Dispatchers.IO) {
            val encryptedBytes = encryptionService.encryptBytes(privateKey, media.content!!)

            val mediaFile = File(mediaDir, media.id)
            FileOutputStream(mediaFile).use { fos ->
                fos.write(encryptedBytes)
                fos.flush()
            }

            MediaContract.saveMedia(
                db,
                media.id,
                privateKey.id,
                encryptedMetadata,
            )
        }
    }

    override suspend fun getMedia(
        privateKey: PrivateKey,
        mediaId: String,
        forceContent: Boolean,
    ): Media? {
        val metadata =
            MediaContract.loadMediaMetadata(db, privateKey.id, mediaId) { cursor ->
                LocalMediaMetadataEnvelope.decrypt(
                    encryptionService,
                    privateKey,
                    cursor.getBlob(cursor.getColumnIndexOrThrow(MediaContract.COLUMN_METADATA)),
                )
            } ?: return null

        return if (forceContent || (metadata.mime.startsWith("image/") && metadata.size < 5 * 1024 * 1024)) {
            val mediaFile = File(mediaDir, mediaId)
            if (!mediaFile.exists()) {
                return null
            }

            try {
                val encryptedBytes = mediaFile.readBytes()
                val decrypted =
                    encryptionService.decryptBytes(
                        privateKey,
                        encryptedBytes,
                    )
                Media(
                    id = mediaId,
                    mimeType = metadata.mime,
                    sizeBytes = metadata.size,
                    content = decrypted,
                )
            } catch (e: Exception) {
                null
            }
        } else {
            Media(
                id = mediaId,
                mimeType = metadata.mime,
                sizeBytes = metadata.size,
                content = null,
            )
        }
    }

    override fun getMime(
        privateKey: PrivateKey,
        mediaId: String,
    ): String? =
        MediaContract
            .loadMediaMetadata(db, privateKey.id, mediaId) { cursor ->
                LocalMediaMetadataEnvelope.decrypt(
                    encryptionService,
                    privateKey,
                    cursor.getBlob(cursor.getColumnIndexOrThrow(MediaContract.COLUMN_METADATA)),
                )
            }?.mime

    override fun deleteMedia(mediaId: String): Boolean {
        val mediaFile = File(mediaDir, mediaId)
        val filesDeleted = mediaFile.delete()
        val deletedRows = MediaContract.deleteMedia(db, mediaId)
        return filesDeleted && deletedRows > 0
    }
}
