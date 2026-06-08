package com.sujana.core.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenModule {
    // Stage 1 replaces this binding with FirebaseTokenProvider
    @Binds
    @Singleton
    abstract fun bindTokenProvider(stub: StubTokenProvider): TokenProvider
}
