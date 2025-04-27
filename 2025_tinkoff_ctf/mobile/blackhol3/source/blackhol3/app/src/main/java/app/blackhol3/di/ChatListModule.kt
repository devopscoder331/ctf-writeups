package app.blackhol3.di

import app.blackhol3.ui.chatlist.ChatListViewModel
import app.blackhol3.ui.pubkey.ImportPublicKeyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatListModule =
    module {
        viewModel { ChatListViewModel(get(), get(), get()) }
        viewModel { ImportPublicKeyViewModel(get(), get()) }
    }
