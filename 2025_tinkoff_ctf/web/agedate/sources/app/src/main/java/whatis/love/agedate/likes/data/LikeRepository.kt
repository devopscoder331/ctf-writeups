package whatis.love.agedate.likes.data

import whatis.love.agedate.api.model.ProfileLikeStatus

interface LikeRepository {
    suspend fun setLike(
        profileID: String,
        status: ProfileLikeStatus,
    ): Result<Boolean>

    suspend fun isLiked(profileID: String): ProfileLikeStatus
}
