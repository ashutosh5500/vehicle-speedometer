package com.example.demoproject.di

import com.example.demoproject.data.SimulatedVehicleDataSource
import com.example.demoproject.data.VehicleDataSource
import com.example.demoproject.data.repository.DefaultVehicleRepository
import com.example.demoproject.domain.repository.VehicleRepository
import com.example.demoproject.domain.usecase.ObserveVehicleStatusUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VehicleModule {
    @Binds
    @Singleton
    abstract fun bindDataSource(source: SimulatedVehicleDataSource): VehicleDataSource

    @Binds
    @Singleton
    abstract fun bindRepository(repository: DefaultVehicleRepository): VehicleRepository

    companion object {
        // Keep the domain layer free of Hilt and javax.inject annotations.
        @Provides
        fun provideObserveVehicleStatus(repository: VehicleRepository): ObserveVehicleStatusUseCase =
            ObserveVehicleStatusUseCase(repository)
    }
}
