package app.blackhol3.di

import app.blackhol3.service.EncryptionService
import app.blackhol3.service.EncryptionServiceImpl
import org.koin.dsl.module

val encryptionModule =
    module {
        single { EncryptionServiceImpl() as EncryptionService }
    }
