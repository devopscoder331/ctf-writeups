package app.blackhol3.di

import app.blackhol3.ui.chat.ChatViewModelFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule =
    module {
        factory { ChatViewModelFactory(get(), get(), get(), get(), get()) }
        viewModel { (chatId: String) -> get<ChatViewModelFactory>().create(chatId) }
    }
