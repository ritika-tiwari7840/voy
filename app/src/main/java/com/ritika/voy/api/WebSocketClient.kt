package com.ritika.voy.api

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketClient(private val url: String) {

    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket
    private val listener = WebSocketListenerImpl()
    val messageChannel = Channel<String>()

    init {
        connect()
    }

    private fun connect() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        Log.d("WebSocketClient", "Sending message: $message")
        webSocket.send(message)
    }

    fun close(code: Int, reason: String) {
        webSocket.close(code, reason)
    }

    private inner class WebSocketListenerImpl : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            Log.d("WebSocket", "Connection opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "Received message: $text")
            runBlocking { messageChannel.send(text) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            val message = bytes.hex()
            Log.d("WebSocket", "Received bytes: $message")
            runBlocking { messageChannel.send(message) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            Log.d("WebSocket", "Connection closing: $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            Log.e("WebSocket", "Error: ${t.message}", t)
        }
    }
}