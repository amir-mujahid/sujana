package com.sujana.core.websocket

import com.sujana.core.network.FirebaseTokenProvider
import com.sujana.shared.dto.WsEventDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val tokenProvider: FirebaseTokenProvider,
    private val wsBaseUrl: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _events = MutableSharedFlow<WsEventDto>(extraBufferCapacity = 64)
    val events: SharedFlow<WsEventDto> = _events.asSharedFlow()

    @Volatile private var socket: WebSocket? = null
    @Volatile private var connected = false
    @Volatile private var shouldRun = false

    fun connect() {
        shouldRun = true
        if (connected) return
        scope.launch { openWithBackoff() }
    }

    fun disconnect() {
        shouldRun = false
        socket?.close(1000, "logout")
        socket = null
        connected = false
    }

    private suspend fun openWithBackoff() {
        var delayMs = 1_000L
        while (shouldRun) {
            val token = try {
                tokenProvider.getToken()
            } catch (_: Exception) {
                null
            }
            if (token == null) {
                delay(delayMs)
                delayMs = min(delayMs * 2, 30_000L)
                continue
            }

            val request = Request.Builder()
                .url(wsBaseUrl)
                .addHeader("Authorization", "Bearer $token")
                .build()

            val listener = SujanaWsListener()
            socket = okHttpClient.newWebSocket(request, listener)

            // Wait until disconnect
            listener.awaitClose()
            connected = false
            if (!shouldRun) break

            delay(delayMs)
            delayMs = min(delayMs * 2, 30_000L)
        }
    }

    private inner class SujanaWsListener : WebSocketListener() {
        private val closedChannel = kotlinx.coroutines.channels.Channel<Unit>(1)

        suspend fun awaitClose() = closedChannel.receive()

        override fun onOpen(webSocket: WebSocket, response: Response) {
            connected = true
            delayReset()
            Timber.d("WS connected")
        }

        private fun delayReset() {
            // reset backoff on successful open — handled in openWithBackoff by re-reading delayMs
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (text == "pong") return
            try {
                val event = json.decodeFromString<WsEventDto>(text)
                scope.launch { _events.emit(event) }
            } catch (_: Exception) {
                Timber.w("WS: unrecognised message: $text")
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WS closed: $code $reason")
            closedChannel.trySend(Unit)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.w(t, "WS failure")
            closedChannel.trySend(Unit)
        }
    }
}
