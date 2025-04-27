package whatis.love.agedate.api.android

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import whatis.love.agedate.IoDispatcher
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.client.AgeDateClient
import whatis.love.agedate.api.client.AgeDateOkHttp
import whatis.love.agedate.api.client.pinning.PinningParametersRepo
import whatis.love.agedate.api.client.session.SessionPersistence
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class APIModule {
    @Provides
    fun provideOkHttpClient(
        pinningParamsRepo: PinningParametersRepo,
        sessionPersistence: SessionPersistence,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): OkHttpClient =
        runBlocking(dispatcher) {
            AgeDateOkHttp(pinningParamsRepo, sessionPersistence)
        }

    @Provides
    @Singleton
    fun provideAPI(
        client: OkHttpClient,
        pinningParametersRepo: PinningParametersRepo,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): AgeDateAPI =
        runBlocking(dispatcher) {
            AgeDateClient(client, pinningParametersRepo)
        }
}
