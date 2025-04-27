package app.blackhol3.data.local.dao

import app.blackhol3.data.local.model.ContentProviderTicket

interface ContentProviderTicketDao {
    fun createTicket(
        ticketID: String,
        privKeyID: String,
        mediaID: String,
    ): ContentProviderTicket

    fun getByID(ticketID: String): ContentProviderTicket?
}
