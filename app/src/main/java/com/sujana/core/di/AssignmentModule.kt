package com.sujana.core.di

import com.sujana.data.repository.AssignmentRepositoryImpl
import com.sujana.domain.repository.IAssignmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AssignmentModule {
    @Binds
    @Singleton
    abstract fun bindAssignmentRepository(impl: AssignmentRepositoryImpl): IAssignmentRepository
}
