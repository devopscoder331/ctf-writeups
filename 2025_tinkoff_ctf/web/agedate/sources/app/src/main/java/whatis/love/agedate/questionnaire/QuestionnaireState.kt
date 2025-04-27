package whatis.love.agedate.questionnaire

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import whatis.love.agedate.api.model.BirthDateVisibility

@Parcelize
data class QuestionnaireState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDay: Int = 0,
    val birthMonth: Int = 0,
    val birthYear: Int = 2015,
    val birthDateVisibility: BirthDateVisibility = BirthDateVisibility.HIDE_NONE,
    val bio: String = "",
    val currentStep: Int = 0,
) : Parcelable
