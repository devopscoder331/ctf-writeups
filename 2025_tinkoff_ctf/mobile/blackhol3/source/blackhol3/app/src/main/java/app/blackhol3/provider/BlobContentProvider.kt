package app.blackhol3.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.net.toUri
import app.blackhol3.repository.ChatRepository
import app.blackhol3.repository.ContentProviderRepository
import app.blackhol3.repository.PrivateKeyRepository
import app.blackhol3.util.toPEM
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream

class BlobContentProvider :
    ContentProvider(),
    KoinComponent {
    private val contentProviderRepository: ContentProviderRepository by inject()
    private val privateKeyRepository: PrivateKeyRepository by inject()
    private val chatRepository: ChatRepository by inject()

    companion object {
        private const val AUTHORITY = "app.blackhol3.provider"
        private const val TICKET = 1
        private const val PUBKEY = 2

        private val uriMatcher =
            UriMatcher(UriMatcher.NO_MATCH).apply {
                addURI(AUTHORITY, "ticket/*", TICKET)
                addURI(AUTHORITY, "pubkey/*", PUBKEY)
            }

        fun getContentUri(ticketId: String): Uri = "content://$AUTHORITY/ticket/$ticketId".toUri()

        fun getPubKeyUri(privateKeyId: String): Uri = "content://$AUTHORITY/pubkey/$privateKeyId".toUri()
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? = null

    private fun getTicketIdFromUri(uri: Uri): String? {
        val pathSegments = uri.pathSegments
        if (pathSegments.size != 2 || pathSegments[0] != "ticket") {
            return null
        }

        return pathSegments[1]
    }

    private fun getPublicKeyIdFromUri(uri: Uri): String? {
        val pathSegments = uri.pathSegments
        if (pathSegments.size != 2 || pathSegments[0] != "pubkey") {
            return null
        }

        return pathSegments[1]
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TICKET -> {
                val ticketId = getTicketIdFromUri(uri) ?: return null
                contentProviderRepository.getMimeByTicket(ticketId)
            }

            PUBKEY -> {
                "application/x-pem-file"
            }

            else -> null
        }
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor? {
        val match = uriMatcher.match(uri)
        return when (match) {
            TICKET -> {
                openTicket(uri)
            }

            PUBKEY -> {
                openPubkey(uri)
            }

            else -> {
                null
            }
        }
    }

    fun openTicket(uri: Uri): ParcelFileDescriptor? {
        val ticketId = getTicketIdFromUri(uri) ?: return null
        val media = contentProviderRepository.getByTicket(ticketId) ?: return null

        return media.content?.let {
            val tempFile = File(context?.cacheDir, "media_ticket_$ticketId")
            FileOutputStream(tempFile).use { it.write(media.content) }
            ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        }
    }

    fun openPubkey(uri: Uri): ParcelFileDescriptor? {
        val pubKeyId = getPublicKeyIdFromUri(uri) ?: return null
        val pubKeyPem =
            privateKeyRepository.getPrivateKey(pubKeyId)?.rsaPublicKey?.toPEM()
                ?: privateKeyRepository.getCurrentPrivateKey()?.let {
                    chatRepository
                        .getChatById(it, pubKeyId)
                        ?.pubKey
                        ?.rsaPublicKey
                        ?.toPEM()
                } ?: return null

        val tempFile = File(context?.cacheDir, "pubkey_$pubKeyId.pem")
        FileOutputStream(tempFile).use { it.write(pubKeyPem.toByteArray()) }
        return ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }
}
