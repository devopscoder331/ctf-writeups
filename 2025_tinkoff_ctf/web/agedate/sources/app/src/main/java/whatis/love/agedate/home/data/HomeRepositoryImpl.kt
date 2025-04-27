package whatis.love.agedate.home.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.await
import whatis.love.agedate.IoDispatcher
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.responses.HomePageContentResponse
import whatis.love.agedate.kv.ActionOnConflict
import whatis.love.agedate.kv.KVStorage
import whatis.love.agedate.profile.data.ProfileRepository
import javax.inject.Inject

private const val KV_TYPE_HOME = "home"
private const val KV_KEY_HOME_CONTENTS = "contents"
private const val KV_KEY_HOME_LIKED = "liked"

class HomeRepositoryImpl
    @Inject
    constructor(
        val kv: KVStorage,
        val api: AgeDateAPI,
        val profileRepository: ProfileRepository,
        @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    ) : HomeRepository {
        override suspend fun getHomeContents(
            justPleaseReturnSomethingThisIsTheFirstRenderGrandmaNeedsToSeeHerBelovedCapybaraFriends: Boolean,
        ): HomePageContentResponse =
            withContext(ioDispatcher) {
                val result =
                    api
                        .home()
                        .await()
                        .toResult()
                        .map {
                            saveHomeContents(it)
                            it
                        }

                val probablyCachedResult =
                    if (justPleaseReturnSomethingThisIsTheFirstRenderGrandmaNeedsToSeeHerBelovedCapybaraFriends) {
                        result.getOrElse {
                            kv.getString(KV_TYPE_HOME, KV_KEY_HOME_CONTENTS)?.let { stored ->
                                Json.Default.decodeFromString<HomePageContentResponse>(stored)
                            } ?: throw it
                        }
                    } else {
                        result.getOrThrow()
                    }

                probablyCachedResult
            }

        override suspend fun getLikedProfiles(useCache: Boolean): List<Profile> =
            withContext(ioDispatcher) {
                val result =
                    api.liked().await().toResult().map {
                        it.liked
                    }

                val probablyCachedResult =
                    if (useCache) {
                        result.getOrElse {
                            kv
                                .getString(KV_TYPE_HOME, KV_KEY_HOME_LIKED)
                                ?.let { Json.decodeFromString<List<Profile>>(it) } ?: throw it
                        }
                    } else {
                        result.getOrThrow()
                    }

                probablyCachedResult
            }

        fun saveHomeContents(contents: HomePageContentResponse) {
            for (profile in contents.randomProfiles) {
                profileRepository.saveProfile(profile)
            }

            kv.setString(
                KV_TYPE_HOME,
                KV_KEY_HOME_CONTENTS,
                Json.Default.encodeToString(contents),
                ActionOnConflict.REPLACE,
            )
        }
    }
