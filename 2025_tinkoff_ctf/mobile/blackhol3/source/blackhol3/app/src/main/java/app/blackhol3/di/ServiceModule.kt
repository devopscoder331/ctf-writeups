package app.blackhol3.di

import android.content.Context
import app.blackhol3.service.MessagePollService
import app.blackhol3.service.MessageUpdateManager
import org.koin.dsl.module

val serviceModule =
    module {
        single { MessagePollService() }
        single { MessageUpdateManager(get<Context>()) }
    }
