package whatis.love.agedate.likes.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LikeModule {
    @Binds
    abstract fun bindLikeRepository(likeRepositoryImpl: LikeRepositoryImpl): LikeRepository
}
