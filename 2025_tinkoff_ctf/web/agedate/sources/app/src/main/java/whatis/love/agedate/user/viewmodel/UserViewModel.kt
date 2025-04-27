package whatis.love.agedate.user.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.MyProfile
import whatis.love.agedate.questionnaire.QuestionnaireState
import whatis.love.agedate.user.data.LoginResult
import whatis.love.agedate.user.data.QuestionnaireResult
import whatis.love.agedate.user.data.UserRepository
import javax.inject.Inject

@HiltViewModel
class UserViewModel
    @Inject
    constructor(
        private val authUserRepository: UserRepository,
    ) : ViewModel() {
        private val _userState = MutableStateFlow<UserState>(UserState.Initial)
        val userState = _userState.asStateFlow()

        private val _questionnaireResult =
            MutableStateFlow<QuestionnaireResult<MyProfile>>(QuestionnaireResult.Initial)
        val questionnaireState = _questionnaireResult.asStateFlow()
        private val _userProfile = MutableStateFlow<MyProfile?>(null)
        val userProfile = _userProfile.asStateFlow()

        private val _userToken = MutableStateFlow<String?>(null)
        val userToken = _userToken.asStateFlow()

        private val _refreshing = MutableStateFlow<Boolean>(false)
        val refreshing = _refreshing.asStateFlow()

        private val _microstatusUpdating = MutableStateFlow<Boolean?>(false)
        val microstatusUpdating = _microstatusUpdating.asStateFlow()

        private val _microstatusError = MutableStateFlow<String?>(null)
        val microstatusError = _microstatusError.asStateFlow()

        val isLoggedIn =
            userState
                .map { state ->
                    state is UserState.Authenticated
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = false,
                )

        init {
            reset()
        }

        fun reset() {
            viewModelScope.launch {
                val currentUser = authUserRepository.getCurrentUser()
                val currentToken = authUserRepository.getAuthToken()
                _userProfile.value = currentUser
                _userToken.value = currentToken
                if (currentUser != null) {
                    if (currentUser.isOnboarded) {
                        _userState.value = UserState.Authenticated
                    } else {
                        _userState.value = UserState.Onboarding
                    }
                } else {
                    _userState.value = UserState.Unauthenticated
                }
            }
        }

        private fun validateCredentials(
            username: String,
            password: String,
        ): Boolean {
            var usernameError: String? = null
            var passwordError: String? = null

            if (username.isBlank()) {
                usernameError = "Не может быть пустым"
            } else if (username.length < 9) {
                usernameError = "Не менее 9 символов"
            }

            if (password.isBlank()) {
                passwordError = "Не может быть пустым"
            } else if (password.length < 9) {
                passwordError = "Не менее 9 символов"
            }

            if (usernameError != null || passwordError != null) {
                _userState.value =
                    UserState.Error(
                        usernameError = usernameError,
                        passwordError = passwordError,
                    )
                return false
            }
            return true
        }

        private fun credentialsAction(
            username: String,
            password: String,
            provider: suspend (String, String) -> LoginResult<MyProfile>,
        ) {
            viewModelScope.launch {
                _userState.value = UserState.Loading
                if (!validateCredentials(username, password)) {
                    return@launch
                }

                when (val result = provider(username, password)) {
                    is LoginResult.Success -> {
                        _userToken.value = authUserRepository.getAuthToken()
                        _userProfile.value = result.data
                        _userState.value =
                            if (result.data.isOnboarded) {
                                UserState.Authenticated
                            } else {
                                UserState.Onboarding
                            }
                    }

                    is LoginResult.Error -> {
                        _userState.value =
                            UserState.Error(
                                message = result.message,
                                usernameError = result.usernameError,
                                passwordError = result.passwordError,
                            )
                    }

                    LoginResult.Loading -> {
                    }
                }
            }
        }

        fun login(
            username: String,
            password: String,
        ) {
            credentialsAction(username, password, authUserRepository::login)
        }

        fun register(
            username: String,
            password: String,
        ) {
            credentialsAction(username, password, authUserRepository::register)
        }

        fun newMicrostatus(text: String) {
            viewModelScope.launch {
                try {
                    _microstatusError.value = null
                    _microstatusUpdating.value = true
                    val result = authUserRepository.newMicrostatus(text)
                    _userProfile.value = result
                } catch (e: Exception) {
                    e.printStackTrace()
                    _microstatusError.value = e.message
                }
                _microstatusUpdating.value = false
            }
        }

        fun onboardingQuestionnaire(questionnaireState: QuestionnaireState) {
            viewModelScope.launch {
                val result = authUserRepository.updateQuestionnaire(questionnaireState)
                _questionnaireResult.value = result
                if (result is QuestionnaireResult.Success) {
                    authUserRepository.updateUserProfile(result.data)
                    _userProfile.value = result.data
                    _userState.value = UserState.Authenticated
                }
            }
        }

        fun resetQuestionnaireState() {
            viewModelScope.launch {
                _questionnaireResult.value = QuestionnaireResult.Initial
            }
        }

        fun refreshUserProfile() {
            viewModelScope.launch {
                _refreshing.value = true
                authUserRepository.refreshUserProfile().fold(
                    onSuccess = { profile ->
                        _userProfile.value = profile
                    },
                    onFailure = {
                    },
                )
                _refreshing.value = false
            }
        }
    }
