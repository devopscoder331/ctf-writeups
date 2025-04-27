package whatis.love.agedate.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.profile.data.ProfileRepository
import javax.inject.Inject

@HiltViewModel
class ProfileQRScreenViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val profileId: String = checkNotNull(savedStateHandle["profileID"])

        private val _uiState = MutableStateFlow(ProfileQRUiState())
        val uiState: StateFlow<ProfileQRUiState> = _uiState.asStateFlow()

        init {
            loadProfile()
        }

        private fun loadProfile() {
            viewModelScope.launch {
                try {
                    _uiState.update {
                        ProfileQRUiState(
                            loading = true,
                            profile = null,
                            errorHeader = null,
                            errorMessage = null,
                        )
                    }
                    val result = profileRepository.getProfileById(profileId, false)
                    _uiState.update {
                        it.copy(
                            profile = result,
                            loading = false,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            profile = null,
                            errorHeader = "Ошибка",
                            errorMessage = e.message ?: "Не удалось загрузить профиль",
                        )
                    }
                }
            }
        }

        fun refresh() {
            loadProfile()
        }
    }

data class ProfileQRUiState(
    val profile: Profile? = null,
    val loading: Boolean = true,
    val errorHeader: String? = null,
    val errorMessage: String? = null,
)
