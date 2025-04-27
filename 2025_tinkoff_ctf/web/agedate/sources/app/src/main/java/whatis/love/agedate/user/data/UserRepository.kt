package whatis.love.agedate.user.data

import whatis.love.agedate.api.model.MyProfile
import whatis.love.agedate.questionnaire.QuestionnaireState

interface UserRepository {
    suspend fun login(
        username: String,
        password: String,
    ): LoginResult<MyProfile>

    suspend fun register(
        username: String,
        password: String,
    ): LoginResult<MyProfile>

    suspend fun updateQuestionnaire(questionnaire: QuestionnaireState): QuestionnaireResult<MyProfile>

    suspend fun newMicrostatus(text: String): MyProfile

    suspend fun logout()

    fun getAuthToken(): String?

    suspend fun getCurrentUser(): MyProfile?

    suspend fun refreshUserProfile(): Result<MyProfile>

    suspend fun updateUserProfile(myProfile: MyProfile)
}
