package whatis.love.agedate.chats.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.chats.data.ChatRepository
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(ChatListUiState())
        val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

        init {
            loadChats()
        }

        private fun loadChats() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                try {
                    val chats = chatRepository.getChats()
                    if (chats != null) {
                        _uiState.update {
                            it.copy(
                                chats = chats,
                                isLoading = false,
                                error = null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Не удалось загрузить сообщения",
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Неизвестная ошибка",
                        )
                    }
                }
            }
        }

        fun refreshChats() {
            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true) }

                try {
                    val chats = chatRepository.getChats()
                    if (chats != null) {
                        _uiState.update {
                            it.copy(
                                chats = chats,
                                isRefreshing = false,
                                error = null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = "Не удалось обновить сообщения",
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = e.message ?: "Неизвестная ошибка",
                        )
                    }
                }
            }
        }
    }

data class ChatListUiState(
    val chats: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)
