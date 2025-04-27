package app.blackhol3.di

import app.blackhol3.data.local.dao.ContentProviderTicketDao
import app.blackhol3.data.local.dao.ContentProviderTicketDaoImpl
import app.blackhol3.repository.ContentProviderRepository
import app.blackhol3.repository.ContentProviderRepositoryImpl
import org.koin.dsl.module

val contentProviderModule =
    module {
        single { ContentProviderTicketDaoImpl(get()) as ContentProviderTicketDao }
        single { ContentProviderRepositoryImpl(get(), get(), get()) as ContentProviderRepository }
    }
