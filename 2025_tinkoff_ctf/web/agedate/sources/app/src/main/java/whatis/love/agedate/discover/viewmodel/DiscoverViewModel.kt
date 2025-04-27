package whatis.love.agedate.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.discover.data.DiscoverRepository
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel
    @Inject
    constructor(
        private val discoverRepository: DiscoverRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DiscoverUIState())
        val uiState: StateFlow<DiscoverUIState> = _uiState.asStateFlow()

        init {
            updateQueue()
        }

        fun updateQueue() {
            viewModelScope.launch {
                try {
                    _uiState.update {
                        it.copy(
                            loading = true,
                        )
                    }
                    val current = uiState.value.profiles
                    val result =
                        discoverRepository
                            .getQueue(uiState.value.evenDisliked)
                            .filter { !current.contains(it) }
                    _uiState.update {
                        it.copy(
                            loading = false,
                            refreshing = false,
                            profiles = current + result,
                            error = null,
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update {
                        it.copy(
                            loading = false,
                            refreshing = false,
                            error = e.message ?: "Пожалуйста, попробуйте позже",
                        )
                    }
                }
            }
        }

        fun fetchMore(threshold: Int = 5) {
            viewModelScope.launch {
                if (uiState.value.profiles.size - uiState.value.currentIndex <= threshold) {
                    updateQueue()
                }
            }
        }
    }

data class DiscoverUIState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val profiles: List<Profile> = emptyList<Profile>(),
    val error: String? = null,
    val evenDisliked: Boolean = false,
    val currentIndex: Int = 0,
)
