package whatis.love.agedate.discover.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.await
import whatis.love.agedate.IoDispatcher
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.profile.data.ProfileRepository
import javax.inject.Inject

class DiscoverRepositoryImpl
    @Inject
    constructor(
        val api: AgeDateAPI,
        val profileRepository: ProfileRepository,
        @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    ) : DiscoverRepository {
        override suspend fun getQueue(includeDisliked: Boolean): List<Profile> =
            withContext(ioDispatcher) {
                api
                    .discover(includeDisliked)
                    .await()
                    .toResult()
                    .map {
                        it.profiles
                    }.getOrThrow()
            }
    }
