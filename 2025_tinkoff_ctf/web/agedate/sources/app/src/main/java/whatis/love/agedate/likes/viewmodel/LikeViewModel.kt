package whatis.love.agedate.likes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import whatis.love.agedate.api.model.ProfileLikeStatus
import javax.inject.Inject

@HiltViewModel
class LikeViewModel
    @Inject
    constructor(
        private val likeManager: LikeManager,
    ) : ViewModel() {
        private var currentProfileId: String? = null
        private var likeStatusFlow: StateFlow<ProfileLikeStatus>? = null

        fun observeLikeStatus(profileId: String): StateFlow<ProfileLikeStatus> {
            if (profileId != currentProfileId || likeStatusFlow == null) {
                currentProfileId = profileId
                likeStatusFlow =
                    likeManager
                        .likedFlow(profileId)
                        .stateIn(
                            viewModelScope,
                            SharingStarted.WhileSubscribed(5000),
                            ProfileLikeStatus.NONE,
                        )
            }
            return likeStatusFlow!!
        }

        fun setLikeStatus(
            profileId: String,
            status: ProfileLikeStatus,
        ) {
            likeManager.setLikeStatus(profileId, status)
        }

        fun like(profileId: String) {
            likeManager.setLikeStatus(profileId, ProfileLikeStatus.LIKED)
        }

        fun dislike(profileId: String) {
            likeManager.setLikeStatus(profileId, ProfileLikeStatus.DISLIKED)
        }
    }
