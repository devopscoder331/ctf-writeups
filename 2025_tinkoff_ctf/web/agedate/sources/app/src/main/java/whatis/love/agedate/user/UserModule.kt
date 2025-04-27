package whatis.love.agedate.user

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import whatis.love.agedate.user.data.UserRepository
import whatis.love.agedate.user.data.UserRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {
    @Binds
    abstract fun provideUserRepository(impl: UserRepositoryImpl): UserRepository
}
