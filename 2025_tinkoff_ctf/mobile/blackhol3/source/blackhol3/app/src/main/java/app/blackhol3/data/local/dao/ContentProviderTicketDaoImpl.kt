package app.blackhol3.data.local.dao

import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.data.local.contract.ContentProviderTicketContract
import app.blackhol3.data.local.model.ContentProviderTicket

class ContentProviderTicketDaoImpl(
    val db: DatabaseHelper,
) : ContentProviderTicketDao {
    override fun createTicket(
        ticketID: String,
        privKeyID: String,
        mediaID: String,
    ): ContentProviderTicket {
        val ticket = ContentProviderTicket(ticketID, privKeyID, mediaID)
        ContentProviderTicketContract.insertTicket(db, ticket)
        return ticket
    }

    override fun getByID(ticketID: String): ContentProviderTicket? = ContentProviderTicketContract.getByID(db, ticketID)
}
