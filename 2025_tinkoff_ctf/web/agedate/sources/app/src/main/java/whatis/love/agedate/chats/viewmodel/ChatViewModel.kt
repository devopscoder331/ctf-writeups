package whatis.love.agedate.chats.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.Message
import whatis.love.agedate.api.model.Profile
import whatis.love.agedate.chats.data.ChatRepository
import whatis.love.agedate.profile.data.ProfileRepository
import whatis.love.agedate.user.data.UserRepository
import javax.inject.Inject

@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val chatRepository: ChatRepository,
        private val profileRepository: ProfileRepository,
        private val userRepository: UserRepository,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val chatId: String = checkNotNull(savedStateHandle["profileId"])
        private val _messages = MutableStateFlow<List<Message>>(emptyList())
        val messages: StateFlow<List<Message>> = _messages.asStateFlow()
        private val _currentUserId = MutableStateFlow<String?>(null)
        val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
        private val _error = MutableStateFlow<String?>(null)
        val error: StateFlow<String?> = _error.asStateFlow()
        private val _matchUser = MutableStateFlow<Profile?>(null)
        val matchUser: StateFlow<Profile?> = _matchUser.asStateFlow()

        init {
            loadCurrentUser()
            loadInitialMessages()
            loadMatchProfile()
        }

        private fun loadCurrentUser() {
            viewModelScope.launch {
                userRepository.getCurrentUser()?.let { user ->
                    _currentUserId.value = user.id
                } ?: run {
                    _error.value = "Failed to get current user"
                }
            }
        }

        private fun loadInitialMessages() {
            viewModelScope.launch {
                _isLoading.value = true

                try {
                    chatRepository
                        .loadInitialMessages(chatId)
                        .onSuccess { messages ->
                            _messages.value = messages
                            _error.value = null
                        }.onFailure {
                            _error.value = "Failed to load messages: ${it.message}"
                        }
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun loadMoreMessages() {
            viewModelScope.launch {
                if (_isLoading.value) return@launch

                _isLoading.value = true

                try {
                    chatRepository
                        .loadMoreMessages(chatId)
                        .onSuccess { newMessages ->
                            _messages.value = chatRepository.getMessages(chatId)
                        }.onFailure {
                            _error.value = "Failed to load more messages: ${it.message}"
                        }
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun sendMessage(text: String) {
            if (text.isBlank()) return

            viewModelScope.launch {
                chatRepository
                    .sendMessage(chatId, text)
                    .onSuccess { message ->
                        _messages.value = chatRepository.getMessages(chatId)
                    }.onFailure {
                        _error.value = "Failed to send message: ${it.message}"
                    }
            }
        }

        private fun loadMatchProfile() {
            viewModelScope.launch {
                try {
                    val profile = profileRepository.getProfileById(chatId, false)
                    _matchUser.value = profile
                } catch (e: Exception) {
                    _error.value = "Failed to load profile: ${e.message}"
                }
            }
        }

        fun refreshMessages() {
            viewModelScope.launch {
                _messages.value = chatRepository.getMessages(chatId)
            }
        }

        fun retryLoadMessages() {
            loadInitialMessages()
        }

        fun clearError() {
            _error.value = null
        }
    }
