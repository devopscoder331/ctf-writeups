package whatis.love.agedate.user.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import retrofit2.await
import whatis.love.agedate.IoDispatcher
import whatis.love.agedate.api.AgeDateAPI
import whatis.love.agedate.api.client.APIFetchFailedException
import whatis.love.agedate.api.client.session.SessionPersistence
import whatis.love.agedate.api.model.MyProfile
import whatis.love.agedate.api.requests.FillQuestionnaireRequest
import whatis.love.agedate.api.requests.RegisterOrLoginRequest
import whatis.love.agedate.api.requests.SetStatusRequest
import whatis.love.agedate.kv.ActionOnConflict
import whatis.love.agedate.kv.KVStorage
import whatis.love.agedate.questionnaire.QuestionnaireState
import javax.inject.Inject

const val KV_TYPE_USER = "user"
const val KV_KEY_USER_PROFILE = "profile"

class UserRepositoryImpl
    @Inject
    constructor(
        private val kv: KVStorage,
        private val api: AgeDateAPI,
        private val sessionPersistence: SessionPersistence,
        @IoDispatcher val ioDispatcher: CoroutineDispatcher,
    ) : UserRepository {
        override suspend fun login(
            username: String,
            password: String,
        ): LoginResult<MyProfile> {
            val request = RegisterOrLoginRequest(username, password)
            return withContext(ioDispatcher) {
                api
                    .login(request)
                    .await()
                    .toResult()
                    .map {
                        val token = it.authToken
                        if (token != null) {
                            sessionPersistence.setAuthorizationToken(token)
                        } else {
                            return@map LoginResult.Error(
                                if (it.usernameError != null || it.passwordError != null) null else "Не удалось войти в аккаунт",
                                it.usernameError,
                                it.passwordError,
                            )
                        }

                        val me = it.me
                        if (me != null) {
                            saveUserProfile(me)
                            return@map LoginResult.Success(me)
                        } else {
                            return@map LoginResult.Error(
                                "Сервер не вернул информацию о профиле",
                                it.usernameError,
                                it.passwordError,
                            )
                        }
                    }.getOrElse {
                        it.printStackTrace()
                        LoginResult.Error(
                            it.message ?: "Неизвестная ошибка",
                        )
                    }
            }
        }

        override suspend fun register(
            username: String,
            password: String,
        ): LoginResult<MyProfile> {
            val request = RegisterOrLoginRequest(username, password)
            return withContext(ioDispatcher) {
                api.register(request).await().toResult().map {
                    val token = it.authToken
                    if (token != null) {
                        sessionPersistence.setAuthorizationToken(token)
                    } else {
                        return@map LoginResult.Error(
                            if (it.usernameError != null || it.passwordError != null) null else "Не удалось получить токен",
                            it.usernameError,
                            it.passwordError,
                        )
                    }

                    val me = it.me
                    if (me != null) {
                        saveUserProfile(me)
                        return@map LoginResult.Success(me)
                    } else {
                        return@map LoginResult.Error(
                            "Сервер не вернул информацию о созданном профиле",
                            it.usernameError,
                            it.passwordError,
                        )
                    }
                }
            }.getOrElse {
                it.printStackTrace()
                LoginResult.Error(
                    it.message ?: "Неизвестная ошибка",
                )
            }
        }

        override suspend fun updateQuestionnaire(questionnaire: QuestionnaireState): QuestionnaireResult<MyProfile> {
            val request =
                FillQuestionnaireRequest(
                    firstName = questionnaire.firstName,
                    lastName = questionnaire.lastName,
                    birthDay = questionnaire.birthDay,
                    birthMonth = questionnaire.birthMonth,
                    birthYear = questionnaire.birthYear,
                    birthDateVisibility = questionnaire.birthDateVisibility,
                    bio = questionnaire.bio,
                )
            return withContext(ioDispatcher) {
                api
                    .questionnaire(request)
                    .await()
                    .toResult()
                    .map {
                        val newMe = it.newMe
                        if (newMe != null) {
                            saveUserProfile(newMe)
                            return@map QuestionnaireResult.Success(newMe)
                        }

                        val error = it.error
                        val firstNameError = it.firstNameError
                        val lastNameError = it.lastNameError
                        val dateError = it.birthDateError
                        val bioError = it.bioError

                        return@map QuestionnaireResult.Error(
                            message = error,
                            firstNameError = firstNameError,
                            lastNameError = lastNameError,
                            dateError = dateError,
                            bioError = bioError,
                        )
                    }.getOrElse {
                        QuestionnaireResult.Error(
                            message = it.message ?: "Неизвестная ошибка",
                        )
                    }
            }
        }

        override suspend fun newMicrostatus(text: String): MyProfile =
            withContext(ioDispatcher) {
                api
                    .setStatus(SetStatusRequest(text))
                    .await()
                    .toResult()
                    .map {
                        it.profile ?: throw APIFetchFailedException(
                            "Ошибка установки статуса",
                            it.error ?: "Неизвестная ошибка",
                        )
                    }.getOrThrow()
            }

        override suspend fun logout() {
            withContext(ioDispatcher) {
                api.logout().await()
            }
            sessionPersistence.setAuthorizationToken("")
            clearUserData()
        }

        override fun getAuthToken(): String? = sessionPersistence.getAuthorizationToken()

        override suspend fun getCurrentUser(): MyProfile? {
            val userJson = kv.getString(KV_TYPE_USER, KV_KEY_USER_PROFILE)
            return userJson?.let {
                try {
                    Json.decodeFromString(MyProfile.serializer(), it)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

        override suspend fun refreshUserProfile(): Result<MyProfile> {
            return withContext(ioDispatcher) {
                api.me().await().toResult().map {
                    val profile = it.me
                    saveUserProfile(profile)
                    return@map profile
                }
            }
        }

        override suspend fun updateUserProfile(myProfile: MyProfile) {
            kv.setString(
                KV_TYPE_USER,
                KV_KEY_USER_PROFILE,
                Json.encodeToString(MyProfile.serializer(), myProfile),
                ActionOnConflict.REPLACE,
            )
        }

        private fun saveUserProfile(myProfile: MyProfile) {
            val userJson = Json.encodeToString(MyProfile.serializer(), myProfile)
            kv.setString(KV_TYPE_USER, KV_KEY_USER_PROFILE, userJson, ActionOnConflict.REPLACE)
        }

        private fun clearUserData() {
            kv.delete(KV_TYPE_USER, KV_KEY_USER_PROFILE)
        }
    }
