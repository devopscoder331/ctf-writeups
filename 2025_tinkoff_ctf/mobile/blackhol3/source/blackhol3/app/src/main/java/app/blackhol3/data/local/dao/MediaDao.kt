package app.blackhol3.data.local.dao

import app.blackhol3.model.Media
import app.blackhol3.model.PrivateKey

interface MediaDao {
    suspend fun saveMedia(
        privateKey: PrivateKey,
        media: Media,
    ): Long

    suspend fun getMedia(
        privateKey: PrivateKey,
        mediaId: String,
        forceContent: Boolean = false,
    ): Media?

    fun getMime(
        privateKey: PrivateKey,
        mediaId: String,
    ): String?

    fun deleteMedia(mediaId: String): Boolean
}
