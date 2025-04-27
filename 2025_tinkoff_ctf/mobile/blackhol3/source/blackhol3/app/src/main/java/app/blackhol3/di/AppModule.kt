package app.blackhol3.di

import app.blackhol3.data.local.DatabaseHelper
import app.blackhol3.repository.PrivateKeyRepository
import app.blackhol3.repository.PrivateKeyRepositoryImpl
import app.blackhol3.service.KeyGenerationService
import app.blackhol3.service.KeyPicGenerationService
import app.blackhol3.service.KeyPicGenerationServiceImpl
import app.blackhol3.ui.common.PrivateKeyViewModel
import app.blackhol3.ui.keygen.KeyGenerationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single { KeyPicGenerationServiceImpl() as KeyPicGenerationService }
        factory { KeyGenerationService(get()) }

        single { DatabaseHelper(get()) }

        includes(
            encryptionModule,
            localDataModule,
            remoteDataModule,
            domainRepositoryModule,
            chatListModule,
            chatModule,
            contentProviderModule,
            serviceModule,
        )

        single { PrivateKeyRepositoryImpl(get(), get()) as PrivateKeyRepository }

        viewModel { PrivateKeyViewModel(get()) }

        viewModel { KeyGenerationViewModel(get(), get()) }
    }
