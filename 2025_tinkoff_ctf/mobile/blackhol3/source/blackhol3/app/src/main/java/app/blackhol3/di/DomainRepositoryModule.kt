package app.blackhol3.di

import app.blackhol3.repository.ChatRepository
import app.blackhol3.repository.ChatRepositoryImpl
import app.blackhol3.repository.MessagesRepository
import app.blackhol3.repository.MessagesRepositoryImpl
import org.koin.dsl.module

val domainRepositoryModule =
    module {
        single { ChatRepositoryImpl(get()) as ChatRepository }
        single { MessagesRepositoryImpl(get(), get(), get(), get()) as MessagesRepository }
    }
