package whatis.love.agedate.profile.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.await
import whatis.love.agedate.IoDispatcher
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.model.Microstatus
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.requests.SetBanRequest
import whatis.love.agedate.kv.ActionOnConflict
import whatis.love.agedate.kv.KVStorage
import javax.inject.Inject

const val KV_TYPE_PROFILE = "profile"
private const val KV_TYPE_PROFILE_STATUSES = "profile_statuses"

class ProfileRepositoryImpl
    @Inject
    constructor(
        val kv: KVStorage,
        val api: AgeDateAPI,
        @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    ) : ProfileRepository {
        override suspend fun getProfileById(
            profileId: String,
            force: Boolean,
        ): Profile {
            return withContext(ioDispatcher) {
                val response = api.profile(profileId).await()
                response
                    .toResult()
                    .map {
                        saveProfile(it.profile)
                        it.profile
                    }.getOrElse { e ->
                        if (!force) {
                            kv
                                .getString(KV_TYPE_PROFILE, profileId, 600)
                                ?.let {
                                    Json.decodeFromString<Profile>(it)
                                }
                        } else {
                            throw e
                        }
                    } ?: throw ProfileNotFoundException("Профиль не найден")
            }

            throw ProfileNotFoundException("Профиль не найден")
        }

        override suspend fun getMicroStatusHistory(
            profileId: String,
            force: Boolean,
        ): List<Microstatus> {
            if (!force) {
                kv
                    .getString(KV_TYPE_PROFILE_STATUSES, profileId, 600)
                    ?.let { return Json.decodeFromString<List<Microstatus>>(it) }
            }

            return withContext(ioDispatcher) {
                val response = api.profileStatuses(profileId).await()
                response
                    .toResult()
                    .map {
                        kv.setString(
                            KV_TYPE_PROFILE_STATUSES,
                            profileId,
                            Json.encodeToString(it.statuses),
                            ActionOnConflict.REPLACE,
                        )
                        return@withContext it.statuses
                    }.getOrThrow()
            }
            return emptyList()
        }

        override fun saveProfile(profile: Profile) {
            kv.setString(
                KV_TYPE_PROFILE,
                profile.id,
                Json.encodeToString(profile),
                ActionOnConflict.REPLACE,
            )
        }

        override suspend fun banProfile(
            profileId: String,
            request: SetBanRequest,
        ): String? =
            withContext(ioDispatcher) {
                api
                    .banProfile(profileId, request)
                    .await()
                    .toResult()
                    .map {
                        if (it.error != null) {
                            it.error
                        } else {
                            null
                        }
                    }.getOrElse {
                        it.message
                    }
            }
    }
