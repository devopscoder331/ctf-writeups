package whatis.love.agedate.profile.viewmodel

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import whatis.love.agedate.api.client.APIFetchFailedException
import whatis.love.agedate.api.model.AdminVisibleProfileFields
import whatis.love.agedate.api.model.Microstatus
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.model.ProfileLikeStatus
import whatis.love.agedate.api.requests.SetBanRequest
import whatis.love.agedate.profile.data.ProfileNotFoundException
import whatis.love.agedate.profile.data.ProfileRepository
import whatis.love.agedate.report.ReportDetails
import whatis.love.agedate.report.ReportPrintService
import whatis.love.agedate.visits.viewmodel.VisitTrackingManager
import javax.inject.Inject

@HiltViewModel
class ProfileScreenViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        private val visitTrackingManager: VisitTrackingManager,
        private val reportPrintService: ReportPrintService,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val profileId: String = checkNotNull(savedStateHandle["profileID"])

        private val _uiState = MutableStateFlow(ProfileUIState())
        val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()
        private val _printErrorMessage = MutableStateFlow<String?>(null)
        val printErrorMessage = _printErrorMessage.asStateFlow()

        private val _banActionSuccess = MutableStateFlow<Boolean?>(null)
        val banActionSuccess: StateFlow<Boolean?> = _banActionSuccess.asStateFlow()

        private val _banActionError = MutableStateFlow<String?>(null)
        val banActionError: StateFlow<String?> = _banActionError.asStateFlow()

        init {
            trackVisit()
            refresh(true)
        }

        fun refresh(initial: Boolean = false) {
            viewModelScope.launch {
                loadProfile(initial)
                loadMicroStatusHistory(initial)
            }
        }

        private suspend fun loadProfile(initial: Boolean = false) {
            try {
                _uiState.update {
                    it.copy(
                        profileLoading = true,
                        profileRefreshing = !initial,
                        profileErrorHeader = null,
                    )
                }
                val result = profileRepository.getProfileById(profileId, !initial)
                _uiState.update {
                    it.copy(
                        profile = result,
                        profileLoading = false,
                        profileErrorHeader = null,
                        profileRefreshing = false,
                    )
                }
            } catch (e: APIFetchFailedException) {
                _uiState.update {
                    it.copy(
                        profileLoading = false,
                        profileRefreshing = false,
                        profileErrorHeader = e.header,
                        profileErrorMessage = e.message ?: "Пожалуйста, попробуйте позже",
                    )
                }
            } catch (e: ProfileNotFoundException) {
                _uiState.update {
                    it.copy(
                        profileLoading = false,
                        profileRefreshing = false,
                        profileErrorHeader = "Не удалось загрузить профиль",
                        profileErrorMessage = e.message ?: "Пожалуйста, попробуйте позже",
                    )
                }
            }
        }

        private suspend fun loadMicroStatusHistory(initial: Boolean = true) {
            try {
                _uiState.update {
                    it.copy(
                        microstatusLoading = true,
                        microstatusError = null,
                    )
                }
                val history = profileRepository.getMicroStatusHistory(profileId, !initial)
                _uiState.update {
                    it.copy(
                        microstatusHistory = history,
                        microstatusLoading = false,
                        microstatusError = null,
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        microstatusLoading = false,
                        microstatusError = e.message ?: "Не удалось загрузить статусы",
                    )
                }
            }
        }

        private fun trackVisit() {
            viewModelScope.launch {
                try {
                    visitTrackingManager.trackVisit(profileId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        fun printReport(
            activity: Activity,
            reportDetails: ReportDetails,
        ) {
            viewModelScope.launch {
                try {
                    reportPrintService.generateAndPrintReport(activity, reportDetails)
                } catch (e: Exception) {
                    e.printStackTrace()
                    _printErrorMessage.value = e.message ?: "Ошибка печати заявления"
                }
            }
        }

        fun setBanStatus(
            profileId: String,
            request: SetBanRequest,
        ) {
            viewModelScope.launch {
                try {
                    val error = profileRepository.banProfile(profileId, request)
                    _banActionSuccess.value = (error == null)
                    _banActionError.value = error
                } catch (e: Exception) {
                    _banActionSuccess.value = false
                    _banActionError.value = e.message
                }
            }
        }

        fun clearPrintError() {
            _printErrorMessage.value = null
        }

        fun clearBanActionResult() {
            _banActionSuccess.value = null
        }
    }

data class ProfileUIState(
    val profile: Profile =
        Profile(
            id = "",
            firstName = "",
            lastName = "",
            birthDay = null,
            birthMonth = null,
            birthYear = null,
            bio = "",
            profilePictureUrl = "",
            microstatus = null,
            likeStatus = ProfileLikeStatus.NONE,
            decidedAt = null,
            likedYouAt = null,
        ),
    val adminFields: AdminVisibleProfileFields? = null,
    val microstatusHistory: List<Microstatus> = emptyList(),
    val profileLoading: Boolean = true,
    val profileErrorHeader: String? = null,
    val profileErrorMessage: String? = null,
    val profileRefreshing: Boolean = false,
    val userIsAdmin: Boolean = true,
    val microstatusLoading: Boolean = true,
    val microstatusError: String? = null,
)
