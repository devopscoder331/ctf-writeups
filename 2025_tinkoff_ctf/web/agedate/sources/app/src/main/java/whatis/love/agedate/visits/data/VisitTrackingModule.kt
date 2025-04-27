package whatis.love.agedate.visits.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class VisitTrackingModule {
    @Binds
    abstract fun bindVisitTrackingRepository(visitTrackingRepositoryImpl: VisitTrackingRepositoryImpl): VisitTrackingRepository
}
