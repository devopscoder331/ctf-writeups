package app.blackhol3.di

import app.blackhol3.data.local.dao.ChatDao
import app.blackhol3.data.local.dao.ChatDaoImpl
import app.blackhol3.data.local.dao.ConfigDao
import app.blackhol3.data.local.dao.LocalMessageDao
import app.blackhol3.data.local.dao.LocalMessageDaoImpl
import app.blackhol3.data.local.dao.MediaDao
import app.blackhol3.data.local.dao.MediaDaoImpl
import app.blackhol3.data.local.dao.PrivateKeyDao
import app.blackhol3.data.local.dao.PrivateKeyDaoImpl
import app.blackhol3.data.local.dao.SharedPreferencesConfigDaoImpl
import org.koin.dsl.module

val localDataModule =
    module {
        single { ChatDaoImpl(get(), get()) as ChatDao }
        single { LocalMessageDaoImpl(get(), get()) as LocalMessageDao }
        single { MediaDaoImpl(get(), get(), get()) as MediaDao }
        single { PrivateKeyDaoImpl(get()) as PrivateKeyDao }
        single { SharedPreferencesConfigDaoImpl(get()) as ConfigDao }
    }
