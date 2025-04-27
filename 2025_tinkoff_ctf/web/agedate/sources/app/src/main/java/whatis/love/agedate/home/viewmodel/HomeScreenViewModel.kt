package whatis.love.agedate.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.api.responses.HomePageContentResponse
import whatis.love.agedate.home.data.HomeRepository
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel
    @Inject
    constructor(
        private val homeRepository: HomeRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(HomeScreenUIState())
        val uiState: StateFlow<HomeScreenUIState> = _uiState.asStateFlow()

        init {
            refresh(true)
        }

        fun refresh(initial: Boolean = false) {
            viewModelScope.launch {
                loadHomeContents(initial)
            }
        }

        private fun loadHomeContents(initial: Boolean = false) {
            viewModelScope.launch {
                try {
                    _uiState.update {
                        it.copy(
                            loading = true,
                            refreshing = !initial,
                        )
                    }
                    val result = homeRepository.getHomeContents(initial)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            refreshing = false,
                            content = result,
                            error = null,
                        )
                    }
                } catch (e: Exception) {
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
    }

data class HomeScreenUIState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val content: HomePageContentResponse? = null,
    val error: String? = null,
    val liked: List<Profile> = emptyList(),
    val likedError: String? = null,
)
