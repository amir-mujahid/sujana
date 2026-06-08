package com.sujana.core.di

import com.sujana.data.repository.RequestRepositoryImpl
import com.sujana.domain.repository.IRequestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RequestModule {
    @Binds
    @Singleton
    abstract fun bindRequestRepository(impl: RequestRepositoryImpl): IRequestRepository
}
