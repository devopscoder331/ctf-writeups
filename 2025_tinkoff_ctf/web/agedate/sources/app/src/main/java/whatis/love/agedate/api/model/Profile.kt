package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Serializable
data class Profile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDay: Int?,
    val birthMonth: Int?,
    val birthYear: Int?,
    val bio: String,
    val profilePictureUrl: String?,
    val microstatus: Microstatus?,
    val likeStatus: ProfileLikeStatus,
    val decidedAt: Long?,
    val likedYouAt: Long?,
) {
    val ageSuffix: String =
        this.birthYear?.let {
            val birthMonth = this.birthMonth
            val birthDay = this.birthDay
            if (birthDay != null && birthMonth != null) {
                val birthDateCalendar = Calendar.getInstance()
                birthDateCalendar.set(it, birthMonth - 1, birthDay)
                val now = Calendar.getInstance()

                val diffInMillis =
                    now.timeInMillis - birthDateCalendar.timeInMillis
                val diffInYears =
                    TimeUnit.MILLISECONDS.toDays(diffInMillis) / 365
                ", $diffInYears"
            } else {
                ", ~" +
                    (
                        Calendar
                            .getInstance()
                            .get(Calendar.YEAR) - it
                    ).toString()
            }
        } ?: ""
}
