package whatis.love.agedate.likes.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.likes.data.LikeRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LikeManager
    @Inject
    constructor(
        private val likeRepository: LikeRepository,
    ) {
        private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val likeStates = ConcurrentHashMap<String, ObservableLikeState>()
        private val matchResults = MutableSharedFlow<Pair<String, Boolean>>(extraBufferCapacity = 10)

        private class ObservableLikeState(
            initialValue: ProfileLikeStatus,
        ) {
            val flow = MutableStateFlow(initialValue)
            val observerCount = AtomicInteger(0)
        }

        fun likedFlow(profileId: String): Flow<ProfileLikeStatus> {
            val state =
                likeStates.getOrPut(profileId) {
                    val newState = ObservableLikeState(ProfileLikeStatus.NONE)

                    managerScope.launch {
                        try {
                            val actualStatus = likeRepository.isLiked(profileId)
                            newState.flow.value = actualStatus
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    newState
                }

            return flow {
                state.observerCount.incrementAndGet()
                try {
                    state.flow.collect { emit(it) }
                } finally {
                    if (state.observerCount.decrementAndGet() == 0) {
                        likeStates.remove(profileId)
                    }
                }
            }
        }

        fun setLikeStatus(
            profileId: String,
            status: ProfileLikeStatus,
        ) {
            managerScope.launch {
                try {
                    privateSetLikeStatus(profileId, status)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        private suspend fun privateSetLikeStatus(
            profileId: String,
            status: ProfileLikeStatus,
        ) {
            likeRepository
                .setLike(profileId, status)
                .map {
                    likeStates[profileId]?.flow?.value = status
                    if (it && status != ProfileLikeStatus.NONE) matchResults.emit(profileId to true)
                }.getOrThrow()
        }
    }
