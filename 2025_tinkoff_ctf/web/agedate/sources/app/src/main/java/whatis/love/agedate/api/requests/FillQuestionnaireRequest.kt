package whatis.love.agedate.api.requests

import kotlinx.serialization.Serializable
import whatis.love.agedate.api.model.BirthDateVisibility

@Serializable
data class FillQuestionnaireRequest(
    val firstName: String,
    val lastName: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val birthDateVisibility: BirthDateVisibility,
    val bio: String,
)
