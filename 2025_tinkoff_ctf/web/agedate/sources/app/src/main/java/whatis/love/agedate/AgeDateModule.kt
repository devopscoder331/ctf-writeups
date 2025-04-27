package whatis.love.agedate

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import whatis.love.agedate.api.client.pinning.PinningParametersPersistence
import whatis.love.agedate.api.client.pinning.PinningParametersRepo
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
class AgeDateModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun providePinningParametersRepo(pinningPersistence: PinningParametersPersistence): PinningParametersRepo =
        PinningParametersRepo(pinningPersistence)
}
