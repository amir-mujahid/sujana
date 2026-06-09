package com.sujana.core.di

import com.sujana.BuildConfig
import com.sujana.core.network.FirebaseTokenProvider
import com.sujana.core.websocket.WebSocketManager
import com.sujana.data.repository.NotificationRepositoryImpl
import com.sujana.domain.repository.INotificationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): INotificationRepository

    companion object {

        @Named("websocket")
        @Provides
        @Singleton
        fun provideWsOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

        @Named("wsUrl")
        @Provides
        @Singleton
        fun provideWsUrl(): String {
            val base = BuildConfig.BASE_URL
            return when {
                base.startsWith("https://") -> "wss://" + base.removePrefix("https://") + "/ws"
                else -> "ws://" + base.removePrefix("http://") + "/ws"
            }
        }

        @Provides
        @Singleton
        fun provideWebSocketManager(
            @Named("websocket") client: OkHttpClient,
            tokenProvider: FirebaseTokenProvider,
            @Named("wsUrl") wsUrl: String,
        ): WebSocketManager = WebSocketManager(client, tokenProvider, wsUrl)
    }
}
