package whatis.love.agedate.api.responses

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.MyProfile

@Serializable
data class FillQuestionnaireResponse(
    val newMe: MyProfile? = null,
    val error: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val birthDateError: String? = null,
    val bioError: String? = null,
)
