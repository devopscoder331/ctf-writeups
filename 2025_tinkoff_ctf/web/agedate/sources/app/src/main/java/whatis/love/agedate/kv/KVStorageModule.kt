package whatis.love.agedate.kv

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import whatis.love.agedate.api.client.pinning.PinningParametersPersistence
import whatis.love.agedate.api.client.session.SessionPersistence
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class KVStorageModule {
    @Provides
    @Singleton
    fun provideKVStorage(
        @ApplicationContext context: Context,
    ): KVStorage = SQLiteBackedKVStorage(context)

    @Provides
    @Singleton
    fun provideSessionPersistence(kv: KVStorage): SessionPersistence = KVSessionPersistence(kv)

    @Provides
    @Singleton
    fun providePinningPersistence(kv: KVStorage): PinningParametersPersistence = KVPinningPersistence(kv)
}
