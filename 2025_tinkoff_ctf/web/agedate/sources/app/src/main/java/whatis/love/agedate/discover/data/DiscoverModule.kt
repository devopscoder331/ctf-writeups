package whatis.love.agedate.discover.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class DiscoverModule {
    @Binds
    abstract fun bindDiscoverRepository(discoverRepositoryImpl: DiscoverRepositoryImpl): DiscoverRepository
}
