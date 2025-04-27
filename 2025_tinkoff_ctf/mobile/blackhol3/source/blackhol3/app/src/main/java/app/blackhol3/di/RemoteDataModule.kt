package app.blackhol3.di

import app.blackhol3.data.remote.MessagingRestClient
import app.blackhol3.data.remote.dao.RemoteMessageDao
import app.blackhol3.data.remote.dao.RemoteMessageDaoImpl
import app.blackhol3.service.MessageWebSocketService
import org.koin.core.qualifier.named
import org.koin.dsl.module

val remoteDataModule =
    module {

        single(qualifier = named("API_BASE_URL")) {
            "https://t-blaster-8azo2gak.spbctf.org"
        }

        single(qualifier = named("JWT_ISSUER")) {
            "app.blackhol3"
        }

        single(qualifier = named("JWT_SUBJECT")) {
            "noid"
        }

        single { RemoteMessageDaoImpl(get(), get(), get()) as RemoteMessageDao }
        single {
            val baseUrl = get<String>(qualifier = named("API_BASE_URL"))
            val issuer = get<String>(qualifier = named("JWT_ISSUER"))
            val subject = get<String>(qualifier = named("JWT_SUBJECT"))

            MessagingRestClient(
                encryptionService = get(),
                baseUrl = baseUrl,
                issuer = issuer,
                subject = subject,
            )
        }

        single {
            MessageWebSocketService(
                encryptionService = get(),
                baseUrl = get(named("API_BASE_URL")),
                issuer = get(named("JWT_ISSUER")),
                subject = get(named("JWT_SUBJECT")),
                messagesRepository = get(),
                privateKeyRepository = get(),
                keyPicGenerationService = get(),
            )
        }
    }
