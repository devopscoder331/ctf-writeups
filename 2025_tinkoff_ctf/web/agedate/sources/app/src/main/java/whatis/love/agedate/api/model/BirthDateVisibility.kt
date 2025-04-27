package whatis.love.agedate.api.model

import kotlinx.serialization.Serializable

@Serializable
enum class BirthDateVisibility {
    HIDE_NONE,
    HIDE_AGE,
    HIDE_BIRTHDAY,
}
