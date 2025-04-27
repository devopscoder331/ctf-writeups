package app.blackhol3.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap

object FileUtil {
    fun getMimeType(
        context: Context,
        uri: Uri,
    ): String? =
        when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> context.contentResolver.getType(uri)
            else -> {
                val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension?.lowercase())
            }
        }

    fun readBytes(
        context: Context,
        uri: Uri,
    ): ByteArray? =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes()
            }
        }.onFailure { it.printStackTrace() }.getOrNull()
}
