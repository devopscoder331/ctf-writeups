package app.blackhol3.repository

import app.blackhol3.data.local.model.ContentProviderTicket
import app.blackhol3.model.Media
import app.blackhol3.model.PrivateKey

interface ContentProviderRepository {
    fun makeTicket(
        privateKey: PrivateKey,
        media: Media,
    ): ContentProviderTicket

    fun getByTicket(ticketID: String): Media?

    fun getMimeByTicket(ticketID: String): String?
}
