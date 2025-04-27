package whatis.love.agedate.visits.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VisitTrackingViewModel
    @Inject
    constructor(
        private val visitTrackingManager: VisitTrackingManager,
    ) : ViewModel() {
        private var currentProfileId: String? = null
        private var profileVisitedFlow: StateFlow<Boolean>? = null

        fun observeProfileVisited(profileId: String): StateFlow<Boolean> {
            if (profileId != currentProfileId || profileVisitedFlow == null) {
                currentProfileId = profileId
                profileVisitedFlow =
                    visitTrackingManager
                        .isVisitedFlow(profileId)
                        .stateIn(
                            viewModelScope,
                            SharingStarted.Companion.WhileSubscribed(5000),
                            false,
                        )
            }
            return profileVisitedFlow!!
        }
    }
