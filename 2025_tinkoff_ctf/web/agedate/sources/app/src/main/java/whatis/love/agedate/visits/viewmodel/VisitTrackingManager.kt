package whatis.love.agedate.visits.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import whatis.love.agedate.visits.data.VisitTrackingRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitTrackingManager
    @Inject
    constructor(
        private val visitTrackingRepository: VisitTrackingRepository,
    ) {
        private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val activeProfiles = ConcurrentHashMap<String, VisitTracker>()

        private class VisitTracker {
            val flow = MutableStateFlow(false)
            val observerCount = AtomicInteger(0)
        }

        fun isVisitedFlow(profileId: String): Flow<Boolean> {
            val tracker =
                activeProfiles.getOrPut(profileId) {
                    VisitTracker().also { newTracker ->
                        managerScope.launch {
                            try {
                                newTracker.flow.value = visitTrackingRepository.profileVisited(profileId)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

            return flow {
                tracker.observerCount.incrementAndGet()

                try {
                    tracker.flow.collect { isVisited ->
                        emit(isVisited)
                    }
                } finally {
                    if (tracker.observerCount.decrementAndGet() == 0) {
                        activeProfiles.remove(profileId)
                    }
                }
            }
        }

        fun trackVisit(profileId: String) {
            managerScope.launch {
                try {
                    visitTrackingRepository.recordVisit(profileId)
                    activeProfiles[profileId]?.flow?.value = true
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
