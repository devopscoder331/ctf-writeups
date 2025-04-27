package app.blackhol3.repository

import app.blackhol3.data.local.dao.ContentProviderTicketDao
import app.blackhol3.data.local.dao.MediaDao
import app.blackhol3.data.local.model.ContentProviderTicket
import app.blackhol3.model.Media
import app.blackhol3.model.PrivateKey
import kotlinx.coroutines.runBlocking
import java.util.UUID

class ContentProviderRepositoryImpl(
    private val dao: ContentProviderTicketDao,
    private val privateKeyRepository: PrivateKeyRepository,
    private val mediaDao: MediaDao,
) : ContentProviderRepository {
    override fun makeTicket(
        privateKey: PrivateKey,
        media: Media,
    ): ContentProviderTicket {
        val ticket =
            dao.createTicket(
                ticketID = UUID.randomUUID().toString(),
                privKeyID = privateKey.id,
                mediaID = media.id,
            )
        return ticket
    }

    override fun getByTicket(ticketID: String): Media? {
        val ticket = dao.getByID(ticketID) ?: return null
        val privateKey = privateKeyRepository.getPrivateKey(ticket.privKeyID) ?: return null
        return runBlocking {
            mediaDao.getMedia(privateKey, ticket.mediaID, forceContent = true)
        }
    }

    override fun getMimeByTicket(ticketID: String): String? {
        val ticket = dao.getByID(ticketID) ?: return null
        val privateKey = privateKeyRepository.getPrivateKey(ticket.privKeyID) ?: return null
        return mediaDao.getMime(privateKey, ticket.mediaID)
    }
}
